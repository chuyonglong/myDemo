


1.前台应用（Foreground App）：
    前台应用是当前用户正在与之交互的应用程序。这是用户当前看到并与之直接互动的应用。
    前台应用通常会占用屏幕，并且用户直接与其进行交互。

2.后台应用（Background App）：
    后台应用是在后台运行的应用程序，用户当前没有直接与之交互。
    后台应用可能仍在运行并执行一些任务，但用户通常无法看到或直接操作它们。

1.前台服务（Foreground Service）：
    前台服务是一种服务，与普通服务不同之处在于它会在通知栏中显示一个持续的通知，以通知用户服务正在运行。
    前台服务通常用于那些需要用户知晓并与之交互的长时间运行操作，例如音乐播放器、导航应用等。

2.后台服务（Background Service）：
    后台服务是一种在后台运行的服务，没有用户界面，用于执行长时间运行的操作。
    后台服务通常用于执行后台任务，如下载文件、处理数据等，而无需用户直接交互。


foregroundServiceType

1.none
    用途: 没有特别指定的前台服务类型。
    引入版本: 原始版本中就存在。
    
2.location
    用途: 与位置相关的服务，例如持续跟踪用户位置的应用程序。
    引入版本: Android 9.0 (API 级别 28)。

3.camera
    用途: 与相机操作相关的服务，例如进行视频录制的服务。
    引入版本: Android 9.0 (API 级别 28)。

4.microphone
    用途: 与麦克风录音相关的服务，例如持续录音的服务。
    引入版本: Android 9.0 (API 级别 28)。

5.phoneCall
    用途: 与电话通话相关的服务，例如处理来电或拨打电话的服务。
    引入版本: Android 9.0 (API 级别 28)。

6.connectedDevice
    用途: 与连接的外部设备相关的服务，例如与蓝牙设备的连接。
    引入版本: Android 12 (API 级别 31)。

7.mediaPlayback
    用途: 与媒体播放相关的服务，例如音乐播放器或视频播放器。
    引入版本: Android 9.0 (API 级别 28)。

8.dataSync
    用途: 与数据同步相关的服务，例如将数据同步到服务器。
    引入版本: Android 10 (API 级别 29)。

9.health
    用途: 与健康监测相关的服务，例如监测设备健康状态。
    引入版本: Android 11 (API 级别 30)。

10.mediaProjection
    用途: 与屏幕录制或截图相关的服务，例如屏幕录制应用。
    引入版本: Android 10 (API 级别 29)。

11.remoteMessaging
    用途: 与远程消息传递相关的服务，例如推送通知的服务。
    引入版本: Android 13 (API 级别 33)。

12.shortService
    用途: 与短时间运行的服务相关的类型，通常用于非常短暂的任务。
    引入版本: Android 12 (API 级别 31)。

13.specialUse
    用途: 特殊用途的前台服务类型，具体用途可以是系统级或特定功能服务。
    引入版本: Android 12 (API 级别 31)。

14.systemExempted
    用途: 系统豁免的服务类型，通常是对系统级服务的特殊处理。
    引入版本: Android 13 (API 级别 33)。
 



为什么要新增foregroundServiceType
1.减少前台服务滥用
2.增强用户透明度与控制
3.电池和性能优化
4.前台服务权限控制
5.符合不同应用场景的灵活性
6.减少对用户干扰

优化前台服务的使用，通过精细化的类型管理来减少滥用行为，提高系统资源的利用效率，保护用户隐私，并增强用户对前台服务的控制感。
这一改进不仅提升了应用的运行效率，也改善了用户的整体体验




要启动前台服务,首先都需要
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

foregroundServiceType android 9引入

android 14 在 AndroidManifest.xml 里面定义的时候必须 确定是什么类型的服务 也就是需要加
foregroundServiceType
android13 至 android10 foregroundServiceType 为可选写入
android 9 以下没有 foregroundServiceType

android 8 必须通过startForegroundService 启动前台服务


如果应用中将 foregroundServiceType 设置为 camera，但实际在服务中实现的不是camera相关的逻辑

1.权限请求不匹配：
foregroundServiceType 主要用于声明服务类型，以便系统可以在后台进行优化。
设置为 camera
表示你的服务会涉及到摄像头操作，这会导致系统要求你申请相应的摄像头权限。如果你的服务实际上并不使用摄像头，
但声明了这一类型，可能会给用户带来混淆，并且不必要的权限请求可能会引起用户的不满。

2.系统优化和行为问题：
Android 系统会根据 foregroundServiceType 的设置对服务进行优化。
如果声明了错误的类型，系统可能会对你的服务进行不适当的优化，影响其正常运行。例如，声明为 camera
的服务可能会被系统认为需要较高的优先级和更多的资源，这在实际应用中可能是不必要的。

3.用户信任问题：用户可能会在看到权限请求或服务说明时对应用产生疑问。如果你的服务与摄像头无关，但声明了
camera 类型，用户可能会质疑应用的透明度和可信度，从而影响应用的信任度。
合理的使用 foregroundServiceType 有助于系统资源的合理分配，也有助于维护用户的信任。













