package com.ebnbin.floatingcamera.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.util.ArrayMap

/**
 * 本地广播帮助类.
 *
 * 以 action 作为发送和接收本地广播的唯一标示.
 */
object LocalBroadcastHelper {
    private val localBroadcastManager = LocalBroadcastManager.getInstance(app)

    /**
     * 每一个实现了 [Receiver] 的类对应一个 [BroadcastReceiver].
     */
    private val broadcastReceivers = ArrayMap<Receiver, BroadcastReceiver>()

    /**
     * 注册监听.
     *
     * @param receiver 通常情况下, 需要接收本地广播的类实现 [Receiver] 接口并作为监听器. 不能重复注册.
     *
     * @param actions 当前 [Receiver] 需要监听的 action 列表. 不能为空.
     */
    fun register(receiver: Receiver, vararg actions: String) {
        if (broadcastReceivers.contains(receiver) || actions.isEmpty()) return

        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                context ?: return
                intent ?: return
                val action = intent.action ?: return
                receiver.onReceive(context, intent, action)
            }
        }
        broadcastReceivers[receiver] = broadcastReceiver

        val filter = IntentFilter()
        actions.forEach { filter.addAction(it) }
        localBroadcastManager.registerReceiver(broadcastReceiver, filter)
    }

    /**
     * 反注册监听.
     */
    fun unregister(receiver: Receiver) {
        val broadcastReceiver = broadcastReceivers.remove(receiver) ?: return

        localBroadcastManager.unregisterReceiver(broadcastReceiver)
    }

    /**
     * 发送一个本地广播.
     *
     * @param action 本地广播唯一标示.
     *
     * @param intent 如果需要额外参数, 创建一个 [Intent] 并传参. 其中 action 会被设置为 [action]. 如果不需要额外参数, 会创建一个
     * action 为 [action] 的 [Intent].
     */
    fun send(action: String, intent: Intent? = null) {
        val validIntent = intent?.apply { this.action = action } ?: Intent(action)
        localBroadcastManager.sendBroadcast(validIntent)
    }

    /**
     * 需要接收本地广播的类需要实现该接口.
     */
    interface Receiver {
        /**
         * 接收本地广播监听.
         *
         * @param action 本地广播唯一标示.
         */
        fun onReceive(context: Context, intent: Intent, action: String)
    }
}
