package com.sjn.stamp.ui.fragment.media

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.sjn.stamp.R
import com.sjn.stamp.controller.StampController
import com.sjn.stamp.ui.DialogFacade
import com.sjn.stamp.ui.SongAdapter
import com.sjn.stamp.ui.item.SongItem
import com.sjn.stamp.utils.LogHelper
import com.sjn.stamp.utils.MediaIDHelper
import com.sjn.stamp.utils.SwipeHelper
import eu.davidea.fastscroller.FastScroller
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager
import eu.davidea.flexibleadapter.helpers.UndoHelper
import java.util.*

class MyStampListFragment : SongListFragment(), UndoHelper.OnUndoListener, FlexibleAdapter.OnItemSwipeListener {

    val categoryValue: String?
        get() = mediaId?.let { MediaIDHelper.extractBrowseCategoryValueFromMediaID(it) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        LogHelper.d(TAG, "onCreateView START" + mediaId!!)
        val rootView = inflater.inflate(R.layout.fragment_list, container, false)

        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(ListFragment.LIST_STATE_KEY)
        }

        loading = rootView.findViewById(R.id.progressBar)
        emptyView = rootView.findViewById(R.id.empty_view)
        fastScroller = rootView.findViewById(R.id.fast_scroller)
        emptyTextView = rootView.findViewById(R.id.empty_text)
        swipeRefreshLayout = rootView.findViewById(R.id.refresh)
        recyclerView = rootView.findViewById(R.id.recycler_view)

        swipeRefreshLayout?.apply {
            setOnRefreshListener(this@MyStampListFragment)
            setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
        }
        adapter = SongAdapter(currentItems, this).apply {
            setNotifyChangeOfUnfilteredItems(true)
            setAnimationOnScrolling(false)
        }
        recyclerView?.apply {
            activity?.let {
                this.layoutManager = SmoothScrollLinearLayoutManager(it).apply {
                    listState?.let { onRestoreInstanceState(it) }
                }
            }
            this.adapter = this@MyStampListFragment.adapter
        }
        adapter?.apply {
            fastScroller = rootView.findViewById<View>(R.id.fast_scroller) as FastScroller
            isLongPressDragEnabled = false
            isHandleDragEnabled = false
            isSwipeEnabled = true
            setUnlinkAllItemsOnRemoveHeaders(false)
            setDisplayHeadersAtStartUp(false)
            setStickyHeaders(false)
            showAllHeaders()
        }
        initializeFabWithStamp()
        if (isShowing) {
            notifyFragmentChange()
        }
        draw()
        LogHelper.d(TAG, "onCreateView END")
        return rootView
    }


    override fun onItemSwipe(position: Int, direction: Int) {
        LogHelper.i(TAG, "onItemSwipe position=" + position +
                " direction=" + if (direction == ItemTouchHelper.LEFT) "LEFT" else "RIGHT")
        val positions = ArrayList<Int>(1)
        positions.add(position)
        val abstractItem = adapter?.getItem(position)
        val message = StringBuilder()
        if (abstractItem?.isSelectable == true) adapter?.setRestoreSelectionOnUndo(false)
        if (direction == ItemTouchHelper.RIGHT) {
            val subItem = abstractItem as SongItem?
            activity?.let { activity ->
                val isCategoryStamp = categoryValue?.let {
                    StampController(activity).isCategoryStamp(it, false, subItem!!.mediaId)
                }
                if (isCategoryStamp == true) {
                    Toast.makeText(activity, R.string.error_message_stamp_failed, Toast.LENGTH_LONG).show()
                    SwipeHelper.cancel(recyclerView, position)
                    return
                }
                message.append(subItem?.title).append(" ")
                DialogFacade.createRemoveStampSongDialog(activity, subItem?.title
                        ?: "", categoryValue
                        ?: "", MaterialDialog.SingleButtonCallback { _, which ->
                    when (which) {
                        DialogAction.NEGATIVE -> {
                            SwipeHelper.cancel(recyclerView, position)
                        }
                        DialogAction.POSITIVE -> {
                            message.append(getString(R.string.action_deleted))
                            swipeRefreshLayout?.isRefreshing = true
                            UndoHelper(adapter, this@MyStampListFragment).apply {
                                withPayload(null) //You can pass any custom object (in this case Boolean is enough)
                                withAction(UndoHelper.ACTION_REMOVE, object : UndoHelper.SimpleActionListener() {
                                    override fun onPostAction() {
                                        // Handle ActionMode title
                                        if (adapter?.selectedItemCount == 0) {
                                            listener?.destroyActionModeIfCan()
                                        } else {
                                            adapter?.let {
                                                listener?.updateContextTitle(it.selectedItemCount)
                                            }
                                        }
                                    }
                                })
                                remove(positions, activity.findViewById(R.id.main_view), message,
                                        getString(R.string.undo), UndoHelper.UNDO_TIMEOUT)
                            }
                        }
                        else -> {
                        }
                    }
                },
                        DialogInterface.OnDismissListener {
                            SwipeHelper.cancel(recyclerView, position)
                        }).show()
            }
        }
    }

    override fun onActionStateChanged(viewHolder: RecyclerView.ViewHolder, actionState: Int) {
        LogHelper.i(TAG, "onActionStateChanged actionState=" + actionState)
        swipeRefreshLayout?.isEnabled = actionState == ItemTouchHelper.ACTION_STATE_IDLE
    }

    override fun onActionCanceled(action: Int) {
        LogHelper.i(TAG, "onUndoConfirmed action=" + action)
        if (action == UndoHelper.ACTION_UPDATE) {
        } else if (action == UndoHelper.ACTION_REMOVE) {
            adapter?.restoreDeletedItems()
            swipeRefreshLayout?.isRefreshing = false
            if (adapter?.isRestoreWithSelection == true) {
                listener?.restoreSelection()
            }
        }
    }

    override fun onActionConfirmed(action: Int, event: Int) {
        LogHelper.i(TAG, "onDeleteConfirmed action=" + action)
        swipeRefreshLayout?.isRefreshing = false
        for (adapterItem in adapter?.deletedItems ?: emptyList()) {
            try {
                when (adapterItem.layoutRes) {
                    R.layout.recycler_song_item -> {
                        val subItem = adapterItem as SongItem
                        activity?.let {
                            StampController(it).run {
                                categoryValue?.let { removeSong(it, false, subItem.mediaId) }
                            }
                            LogHelper.i(TAG, "Confirm removed " + subItem.toString())
                        }
                    }
                }
            } catch (ignored: IllegalStateException) {
            }
        }
    }

    companion object {
        private val TAG = LogHelper.makeLogTag(MyStampListFragment::class.java)
    }

}
