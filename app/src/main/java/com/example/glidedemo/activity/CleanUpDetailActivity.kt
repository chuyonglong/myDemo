package com.example.glidedemo.activity

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.glidedemo.R
import com.example.glidedemo.adapter.CleanUpDetailAdapter
import com.example.glidedemo.bean.BaseData
import com.example.glidedemo.bean.CleanUpDetailData
import com.example.glidedemo.bean.ScanMediaData
import com.example.glidedemo.bean.SimilarImagesData
import com.example.glidedemo.databinding.ActivityCleanUpDetailBinding
import com.example.glidedemo.databinding.DialogCleanupDeleteProgressBinding
import com.example.glidedemo.dialog.DeleteDialog
import com.example.glidedemo.dto.DialogDTO
import com.example.glidedemo.entity.CleanUpDetailTitleData
import com.example.glidedemo.extensions.byte2FitMemorySizeToString
import com.example.glidedemo.extensions.doSendBroadcast
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.utils.CleanTypeEnum
import com.example.glidedemo.utils.CleanupFunctionUtils
import com.example.glidedemo.utils.DataQueueManager
import com.example.glidedemo.utils.DialogClickEnum
import com.example.glidedemo.utils.MediaTypeEnum
import com.example.glidedemo.utils.beGone
import com.example.glidedemo.utils.beVisible
import com.example.glidedemo.utils.isGone
import com.example.glidedemo.utils.isVisible
import com.example.glidedemo.utils.similarImagesDB
import com.example.glidedemo.views.GalleryGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class CleanUpDetailActivity : AppCompatActivity(), CleanUpDetailAdapter.OnItemClickListener {
    private val binding: ActivityCleanUpDetailBinding by lazy {
        ActivityCleanUpDetailBinding.inflate(layoutInflater)

    }
    private val cleanUpDetailAdapter: CleanUpDetailAdapter by lazy {
        CleanUpDetailAdapter()
    }

    //最终数据 标题+数据 一个list
    private var similarList: CopyOnWriteArrayList<BaseData> = CopyOnWriteArrayList()

    private val DELETE_FILE_SDK_30_HANDLER = 302

    //默认相似度
    private var SIMILARITY = 85


    //已分组map,查找分组
    private val mediumGroups = ConcurrentHashMap<String, CopyOnWriteArrayList<CleanUpDetailData>>()

    //相似图片key 列表
    private val keyList = CopyOnWriteArrayList<String>()

    //未分组list
    private var notSimilarList: MutableList<CleanUpDetailData> = mutableListOf()

    //数据查询类
    private val cleanupFunctionUtils = CleanupFunctionUtils()

    private var cleanType: String? = null

    //删除文件大小
    private var jumpSizeStr = ""

    //是否正在只能选择
    private var isLoading: Boolean = false

    private val selectedList = ArrayList<CleanUpDetailData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupAdapter()
        cleanType = intent.getStringExtra("type")
        binding.apply {
            similarPhotoToolbar.setNavigationOnClickListener {
                finish()
            }
            deleteButton.setOnClickListener {
                if (isLoading) {
                    toast(getString(R.string.smart_choice_progress))
                    return@setOnClickListener
                }
                deleteSimilar()
            }

        }
        cleanType?.let {
            if (cleanType == CleanTypeEnum.TYPE_SCREENSHOT.name) {
                binding.similarPhotoToolbar.title = getString(R.string.screenshot)
                binding.cbSmartChoice.beGone()
                binding.tvSmartChoice.beGone()
                binding.cleanupTips.beGone()
            } else {
                binding.cleanupTips.beVisible()
                binding.similarPhotoToolbar.title = getString(R.string.similar_photos)
                binding.smartChoiceButton.setOnClickListener {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (isLoading) return@launch
                        if (binding.cbSmartChoice.isSelected) {
                            cancelSmartChoice()
                        } else {
                            smartChoice()
                        }
                    }
                }
            }
            getData()
        }
    }


    /**
     * 获取数据
     */
    private fun getData() {
        similarList.clear()
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isLoading = true
                binding.similarRefresh.isRefreshing = true
            }
            if (cleanType == CleanTypeEnum.TYPE_SIMILAR.name) {
                val list = CopyOnWriteArrayList(similarImagesDB.getAllGroupSimilar())
                if (list.isNotEmpty()) {
                    groupList(list)
                    //智能选择
//                    smartChoice()

                }
            } else if (cleanType == CleanTypeEnum.TYPE_SCREENSHOT.name) {
                try {
                    getScreenshotImages()
                } catch (e: Exception) {
                }
            }
            withContext(Dispatchers.Main) {
                if (similarList.isEmpty()) {
                    if (binding.tvNoSimilarText.isGone()) {
                        val noSimilarText = when (cleanType) {
                            CleanTypeEnum.TYPE_SIMILAR.name -> getString(R.string.empty_similar)
                            CleanTypeEnum.TYPE_SCREENSHOT.name -> getString(R.string.empty_screenshots)
                            else -> getString(R.string.no_images)
                        }
                        binding.tvNoSimilarText.beVisible()
                        binding.tvNoSimilarText.text = noSimilarText
                        binding.ivNoSimilarImage.beVisible()
                        binding.deleteButton.beGone()
                    }
                } else {
                    if (binding.tvNoSimilarText.isVisible()) {
                        binding.tvNoSimilarText.beGone()
                        binding.ivNoSimilarImage.beGone()
                        binding.deleteButton.beVisible()
                    }
                    cleanUpDetailAdapter.overallRefresh(similarList)
                }
                isLoading = false
                binding.similarRefresh.isRefreshing = false
            }
        }
    }


    private fun setupAdapter() {
        cleanUpDetailAdapter.setOnItemClickListener(this)
        binding.rvSimilar.adapter = cleanUpDetailAdapter
        setupGridLayoutManager()
    }


    private fun setupGridLayoutManager() {
        val layoutManager = GalleryGridLayoutManager(this,4)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (cleanUpDetailAdapter.isSimilarTitle(position)) {
                    layoutManager.spanCount
                } else {
                    1
                }
            }
        }
    }

    private fun deleteSimilar() {
        val selectList = cleanUpDetailAdapter.getSelectedItem()
        if (selectList.isEmpty()) {
            toast(getString(R.string.nothing_item_selected))
            return
        }
        val dialogDTO = DialogDTO(
            content = getString(R.string.delete_content),
            cancelText = R.string.cancel,
            okText = R.string.delete
        )
        DeleteDialog(this, dialogDTO) {
            if (it == DialogClickEnum.OK) {
                lifecycleScope.launch(Dispatchers.IO) {
                    deleteItemSelected(selectList)
                }
            }
        }.show()
    }

    private suspend fun deleteItemSelected(selectedItems: ArrayList<CleanUpDetailData>) {
        if (selectedItems.isEmpty()) {
            toast(getString(R.string.nothing_item_selected))
            return
        }
        tryDeleteSimilar(selectedItems) {
            if (it) {
                lifecycleScope.launch(Dispatchers.IO) {
                    deleteSimilar(selectedItems)
                }
            }
        }
    }

    /**
     * 尝试删除相似图片,权限请求
     */
    private suspend fun tryDeleteSimilar(
        selectedItems: ArrayList<CleanUpDetailData>, callback: (success: Boolean) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uriList = selectedItems.map { it.uri }
            try {
                withContext(Dispatchers.Main) {
                    val writeRequest =
                        MediaStore.createDeleteRequest(contentResolver, uriList).intentSender
                    cleanUpLauncher.launch(IntentSenderRequest.Builder(writeRequest).build())
                    selectedList.clear()
                    selectedList.addAll(selectedItems)

                }
            } catch (e: Exception) {
            }
        } else {
            deleteSimilar(selectedItems)
        }
    }

    private var bottomSheetDialog: BottomSheetDialog? = null

    /**
     * 删除
     */
    private suspend fun deleteSimilar(selectedItems: ArrayList<CleanUpDetailData>) {
        val bottomBinding = DialogCleanupDeleteProgressBinding.inflate(layoutInflater)
        withContext(Dispatchers.Main) {
            bottomBinding.messageTextView.text = getString(R.string.cleaning_up)
            bottomSheetDialog = BottomSheetDialog(this@CleanUpDetailActivity)
            bottomBinding.apply {
                bottomSheetDialog?.setCancelable(false)
                bottomSheetDialog?.setCanceledOnTouchOutside(false)
                bottomSheetDialog?.setContentView(this.root)
                if (selectedItems.size > 1) {
                    bottomSheetDialog?.show()
                }
                loadingProgress.max = selectedItems.size
                loadingProgress.progress = 0
                tvCount.text = getString(R.string.loading_count, "0", selectedItems.size.toString())
                loadingClosure.beGone()
            }
        }
        val deleteList = arrayListOf<String>()
        for ((index, item) in selectedItems.withIndex()) {
            val filePath = item.path
            if (File(filePath).delete()) {
                deleteList.add(filePath)
                withContext(Dispatchers.Main) {
                    bottomBinding.loadingProgress.progress = index
                    bottomBinding.tvCount.text = getString(
                        R.string.loading_count,
                        index.toString(),
                        selectedItems.size.toString()
                    )
                }
            } else {
                Log.d("223366", "deleteSimilar: File  delete fail")
            }
        }
        withContext(Dispatchers.Main) {
            updateDeleteButton(null, 0)
        }
        sortAndRefreshUI(deleteList)

    }

    /**
     * 删除数据之后 排序重组刷新ui
     */
    private suspend fun sortAndRefreshUI(deleteList: ArrayList<String>) {
        val tempList = similarList.clone() as CopyOnWriteArrayList<BaseData>
        val iterator = tempList.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item is CleanUpDetailTitleData) {
                continue
            }
            val filePath = (item as CleanUpDetailData).path
            if (deleteList.contains(filePath)) {

                //数据库数据删除
                if (cleanType == CleanTypeEnum.TYPE_SIMILAR.name) {
                    similarImagesDB.deleteSimilarByMediaStoreId(item.id)
                }
                doSendBroadcast(filePath)
                tempList.remove(item)
                DataQueueManager.updateSimilarData(cleanType!!, item, arrayListOf())
            }

        }
        val finalList = if (cleanType == CleanTypeEnum.TYPE_SIMILAR.name) {
            deleteSimilarEmptyTitle(tempList)
        } else {
            deleteScreenshotEmptyTitle(tempList)
        }
        withContext(Dispatchers.Main) {
            val intent = Intent(this@CleanUpDetailActivity, CleanupFinishActivity::class.java)
            val typeStr = if (cleanType == CleanTypeEnum.TYPE_SIMILAR.name) {
                getString(R.string.similar_photos)
            } else {
                getString(R.string.screenshot)
            }
            bottomSheetDialog?.hide()
            bottomSheetDialog?.dismiss()
            if (finalList.isEmpty()) {
                DataQueueManager.updateSimilarData(cleanType!!, null, arrayListOf())
                intent.apply {
                    putExtra("sizeStr", jumpSizeStr)
                    putExtra("jumpHome", true)
                    startActivity(this)
                }
                finish()
                0
            } else {
                similarList.clear()
                similarList.addAll(finalList)
                cleanUpDetailAdapter.overallRefresh(similarList)
                val similarPhotoDataList = similarList.filterIsInstance<CleanUpDetailData>()
                    .take(minOf(similarList.size, 5))
                val scanPhotoDataList = arrayListOf<ScanMediaData>()
                if (similarPhotoDataList.isNotEmpty()) {
                    for (data in similarPhotoDataList) {
                        scanPhotoDataList.add(
                            ScanMediaData(
                                data.name, data.path, data.size, data.stamp,
                                MediaTypeEnum.TYPE_IMAGES
                            )
                        )
                    }
                }
                DataQueueManager.updateSimilarData(cleanType!!, null, scanPhotoDataList)
                intent.apply {
                    putExtra("type", typeStr)
                    putExtra("sizeStr", jumpSizeStr)
                    putExtra("jumpHome", false)
                    startActivity(this)
                }
            }
        }


    }

    /**
     * 删除空的分组 ,或者只有一条数据的分组
     */
    private suspend fun deleteSimilarEmptyTitle(tempList: CopyOnWriteArrayList<BaseData>): CopyOnWriteArrayList<BaseData> {
        val finalList = CopyOnWriteArrayList<BaseData>()
        val indexList = mutableSetOf<Int>()
        for (index in 0 until tempList.size - 1) {
            val item = tempList[index]
            if (item is CleanUpDetailTitleData) {
                //当前item 为标题
                val lastIndex = tempList.size - 1
                //上面 lastIndex 已经减去1 ,下面的 -1 代表总长度 -2
                if (index == lastIndex - 1) {
                    //当前最后一组只有一条数据
                    val similarPhotoData = tempList.last() as? CleanUpDetailData
                    if (similarPhotoData != null) {
                        DataQueueManager.updateSimilarData(
                            cleanType!!, similarPhotoData, arrayListOf()
                        )
                        similarImagesDB.deleteSimilarByMediaStoreId(similarPhotoData.id)
                    }
                    indexList.add(lastIndex)
                    indexList.add(lastIndex - 1)
                    break
                } else if (index == lastIndex) {
                    //最后一条为标题
                    indexList.add(lastIndex)
                    break
                } else {
                    val nextItem = tempList[index + 1]
                    val twoIndex = tempList[index + 2]
                    if (twoIndex is CleanUpDetailTitleData) {
                        indexList.add(index)
                        indexList.add(index + 1)
                        val similarPhotoData = tempList[index + 1]
                        if (similarPhotoData is CleanUpDetailData) {
                            similarImagesDB.deleteSimilarByMediaStoreId(similarPhotoData.id)
                            val similarPhotoDataList =
                                tempList.filterIsInstance<CleanUpDetailData>()
                                    .take(minOf(tempList.size, 5))
                            val scanPhotoDataList = arrayListOf<ScanMediaData>()
                            if (similarPhotoDataList.isNotEmpty()) {
                                for (data in similarPhotoDataList) {
                                    scanPhotoDataList.add(
                                        ScanMediaData(
                                            data.name, data.path, data.size, data.stamp,
                                            MediaTypeEnum.TYPE_IMAGES
                                        )
                                    )
                                }
                            }
                            DataQueueManager.updateSimilarData(
                                cleanType!!, similarPhotoData, arrayListOf()
                            )
                        }
                    } else if (nextItem is CleanUpDetailTitleData) {
                        indexList.add(index)
                    }
                }
            }
        }
        indexList.sortedDescending().forEach {
            tempList.removeAt(it)
        }

        //重新排序
        var groupCount = 1
        for (item in tempList) {
            if (item is CleanUpDetailTitleData) {
                val newGroupName = getString(R.string.similar_set, groupCount.toString())
                finalList.add(CleanUpDetailTitleData(newGroupName))
                groupCount++
            } else {
                finalList.add(item)
            }
        }
        return finalList
    }

    private fun deleteScreenshotEmptyTitle(tempList: CopyOnWriteArrayList<BaseData>): CopyOnWriteArrayList<BaseData> {
        val cloneList = tempList.clone() as CopyOnWriteArrayList<BaseData>
        for (index in 0 until tempList.size) {
            val item = tempList[index]
            if (item is CleanUpDetailTitleData) {
                val nextItem = tempList.getOrNull(index + 1)
                if (nextItem == null) {
                    cloneList.remove(item)
                    break
                }
                if (nextItem is CleanUpDetailTitleData) {
                    cloneList.remove(item)
                }
            }
        }
        return cloneList
    }


    /**
     * 选择框点击回调
     */
    override fun onItemClick(item: CleanUpDetailData) {
        if (isLoading) {
            if (item.isSelected) {
                toast(getString(R.string.smart_choice_progress))
            }
            return
        }
        cleanUpDetailAdapter.getSelectedSize { count, sizeStr ->
            if (count > 0) {
                updateDeleteButton(sizeStr, count)
            } else {
                updateDeleteButton(null, 0)
            }

        }
    }

    /**
     * 图片点击回调
     */
    override fun onImageClick(item: CleanUpDetailData) {
//        Intent(this, MediaDetailActivity::class.java).apply {
//            putExtra(PATH, item.path)
//            putExtra(MEDIA_FILES_CHOOSE, MediaDetailDataTypeEnum.MEDIA_FILES_CLEANUP.name)
//            startActivity(this)
//        }
    }


    /**
     * 获取截图
     */

    private suspend fun getScreenshotImages() {

    }

    /**
     * 比较是否相似
     */
    var smartChoiceSize = 0L
    var smartChoiceCount = 0
    private suspend fun getFinalList(list: List<SimilarImagesData>) {
        for (similarData in list) {
            if (!File(similarData.imagePath).exists()) {
                similarImagesDB.deleteSimilarByMediaStoreId(similarData.mediaStoreId)
                continue
            }
            val mediaUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.getContentUri("external"), similarData.mediaStoreId
            )
            val similarPhotoData = CleanUpDetailData(
                similarData.imageName,
                similarData.imagePath,
                similarData.size,
                similarData.createTime,
                mapOf("hash" to similarData.bitmapHash),
                similarData.mediaStoreId,
                false,
                mediaUri
            )

            //临时value
            var data = CopyOnWriteArrayList<CleanUpDetailData>()
            //下标
            var myPosition = 0
            var key: String? = null
            if (mediumGroups.isNotEmpty()) {
                //已分组数据不为空,有相同的分组 临时 key value 赋值
                for (itemKey in keyList) {
                    val group = mediumGroups[itemKey] ?: continue
                    // 这里+1 是要加入title +1不能删除
                    myPosition += group.size + 1
                    val similarity =
                        cleanupFunctionUtils.checkSimilar(similarPhotoData, group[0], SIMILARITY)
                    if (similarity) {
                        similarPhotoData.isSelected = true
                        smartChoiceSize += similarPhotoData.size
                        smartChoiceCount++
                        group.add(similarPhotoData)
                        data = group
                        key = itemKey
                        if (myPosition > similarList.size) {
                            similarList.add(similarPhotoData)
                        } else {
                            similarList.add(myPosition, similarPhotoData)
                        }
                        withContext(Dispatchers.Main) {
                            cleanUpDetailAdapter.localRefresh(similarList, myPosition)
                        }
                        myPosition++
                        break
                    }
                }
            }
            val uuid: String = UUID.randomUUID().toString()
            if (TextUtils.isEmpty(key) && notSimilarList.isNotEmpty()) {
                //没有相同的已分组数据 ,且未分组数据不为空,查找不分组数据里面的相同数据 有数据 临时 key value 赋值
                val notSimilarListCopy = notSimilarList.toList()
                for (item in notSimilarListCopy) {
                    val similarity =
                        cleanupFunctionUtils.checkSimilar(similarPhotoData, item, SIMILARITY)
                    if (similarity) {
                        similarPhotoData.isSelected = true
                        smartChoiceSize += similarPhotoData.size
                        smartChoiceCount++
                        val groupKey = createNewGroupName()
                        keyList.add(groupKey)
                        key = groupKey
                        data.add(item)
                        data.add(similarPhotoData)
                        similarList.add(CleanUpDetailTitleData(groupKey))
                        similarList.add(item)
                        similarList.add(similarPhotoData)
                        //未分组里面也去掉
                        notSimilarList.remove(item)
                        withContext(Dispatchers.Main) {
                            cleanUpDetailAdapter.localRefresh(similarList)
                        }
                        break
                    }
                }
            }
            if (TextUtils.isEmpty(key)) {
                //没有相同的分组 未分组数据 添加
                notSimilarList.add(similarPhotoData)
            } else {
                //有相同的分组 ,以分组数据添加
                mediumGroups[key!!] = data
            }

        }
        //已选择的数据更新按钮
        withContext(Dispatchers.Main) {
            val sizeStr = smartChoiceSize.byte2FitMemorySizeToString()
            updateDeleteButton(sizeStr, smartChoiceCount)
            binding.cbSmartChoice.isClickable = true
            binding.cbSmartChoice.isSelected = true
        }
    }

    private var autoGroupName = 0

    private fun createNewGroupName(): String {
        autoGroupName++
        return getString(R.string.similar_set, autoGroupName.toString())
    }

    /**
     * 相似图片智能选择
     */
    private suspend fun smartChoice() {
        withContext(Dispatchers.Main) {
            binding.cbSmartChoice.isClickable = false
        }
        var count = 0
        var size = 0L
        if (similarList.isEmpty()) {
            return
        }
        for ((index, item) in similarList.withIndex()) {
            if (item is CleanUpDetailData) {
                //当前一条不为标题则选中
                val previousItem = similarList.getOrNull(index - 1)
                if (previousItem is CleanUpDetailData) {
                    item.isSelected = true
                    count++
                    size += item.size
                } else {
                    item.isSelected = false
                }
            }
        }
        withContext(Dispatchers.Main) {
            cleanUpDetailAdapter.overallRefresh(similarList)
            val sizeStr = size.byte2FitMemorySizeToString()
            updateDeleteButton(sizeStr, count)
            binding.cbSmartChoice.isClickable = true
            binding.cbSmartChoice.isSelected = true

        }
    }

    /**
     * 取消智能选择
     */
    private suspend fun cancelSmartChoice() {
        if (similarList.isEmpty()) {
            return
        }
        isLoading = true
        withContext(Dispatchers.Main) {
            binding.similarRefresh.isRefreshing = true
        }

        similarList.forEach {
            if (it is CleanUpDetailData) {
                it.isSelected = false
            }
        }
        withContext(Dispatchers.Main) {
            cleanUpDetailAdapter.overallRefresh(similarList)
            updateDeleteButton(null, 0)
            binding.cbSmartChoice.isSelected = false
            isLoading = false
            binding.similarRefresh.isRefreshing = false
        }
    }


    /**
     * 更新删除按钮
     */
    private fun updateDeleteButton(sizeStr: String?, count: Int) {
        //点击按钮后 去掉只能选择框
        binding.cbSmartChoice.isSelected = false
        binding.similarPhotoToolbar.apply {
            title = if (count == 0) {
                if (cleanType == CleanTypeEnum.TYPE_SCREENSHOT.name) {
                    getString(R.string.screenshot)
                } else {
                    getString(R.string.similar_photos)
                }
            } else {
                getString(R.string.items_selected, count, sizeStr)
            }
        }

//        binding.deleteButton.apply {
//            isClickable = count != 0
//            if (sizeStr != null) {
//                jumpSizeStr = sizeStr
//                text = getString(R.string.similar_cleanup_button, sizeStr)
//                backgroundTintList =
//                    ColorStateList.valueOf(resources.getColor(R.color.vault_select))
//                setTextColor(Color.parseColor("#ffffff"))
//            } else {
//                text = getString(R.string.select_cleanup)
//                backgroundTintList =
//                    ColorStateList.valueOf(resources.getColor(R.color.grey_lock))
//                setTextColor(Color.parseColor("#424242"))
//            }
//        }

    }

    private fun groupList(list: List<SimilarImagesData>) {
        val similarGroupMap = LinkedHashMap<String, ArrayList<CleanUpDetailData>>()
        for (item in list) {
            if (!File(item.imagePath).exists()) {
                similarImagesDB.deleteSimilarByMediaStoreId(item.mediaStoreId)
                continue
            }
            val mediaUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.getContentUri("external"), item.mediaStoreId
            )
            val similarPhotoData = CleanUpDetailData(
                item.imageName,
                item.imagePath,
                item.size,
                item.createTime,
                mapOf("hash" to item.bitmapHash),
                item.mediaStoreId,
                false,
                mediaUri
            )
            val key = item.groupId ?: continue
            if (!similarGroupMap.containsKey(key)) {
                similarGroupMap[key] = java.util.ArrayList()
            }
            similarGroupMap[key]!!.add(similarPhotoData)
        }
        for ((_, value) in similarGroupMap) {
            val groupKey = createNewGroupName()
            similarList.add(CleanUpDetailTitleData(groupKey))
            similarList.addAll(value)
        }
    }

    private val cleanUpLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
//                lifecycleScope.launch(Dispatchers.IO) {
//                    if (selectedList.isNotEmpty())
//                deleteSimilar(selectedList)
//                }
                updateDeleteButton(null, 0)
                lifecycleScope.launch(Dispatchers.IO) {
                    val deleteList = arrayListOf<String>()
                    for ((index, item) in selectedList.withIndex()) {
                        deleteList.add(item.path)
                    }
                    sortAndRefreshUI(deleteList)
                }
            }
        }
}
