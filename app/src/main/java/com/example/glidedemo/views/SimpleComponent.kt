package com.example.glidedemo.views

import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.binioter.guideview.Component
import com.example.glidedemo.databinding.LayerFrendsBinding


class SimpleComponent : Component {
    override fun getView(inflater: LayoutInflater): View {
        val layerFriends = LayerFrendsBinding.inflate(inflater)
        layerFriends.root.setOnClickListener { view ->
            Toast.makeText(
                view.context, "引导层被点击了", Toast.LENGTH_SHORT
            ).show()
        }

        return layerFriends.root
    }

    override fun getAnchor(): Int {
        return Component.ANCHOR_BOTTOM
    }

    override fun getFitPosition(): Int {
        return Component.FIT_END
    }

    override fun getXOffset(): Int {
        return 0
    }

    override fun getYOffset(): Int {
        return 10
    }
}
