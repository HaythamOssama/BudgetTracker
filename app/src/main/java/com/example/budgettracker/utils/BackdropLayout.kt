package com.example.budgettracker.utils

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.animation.doOnEnd
import com.example.budgettracker.R
import com.example.budgettracker.ui.ui.expensesviewer.BackdropToolbar
import top.defaults.drawabletoolbox.DrawableBuilder

@RequiresApi(Build.VERSION_CODES.Q)
class BackdropLayout @JvmOverloads constructor(context: Context, attribute : AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context,attribute,defStyleAttr)
{
    private var backdropToolbarId: Int = 0
    private var backLayoutContainerId: Int = 0
    private var frontLayoutId: Int = 0

    private lateinit var backdropToolbar: BackdropToolbar
    private lateinit var backLayoutContainer: FrameLayout
    private lateinit var frontLayoutContainer: View

    private var frontViewInBackLayout: View ?= null
    private var frontLayoutCornerRadiusDp : Int = 16

    private var disableViewDrawableBuilder: DrawableBuilder

    init {
        val typedArray = context.obtainStyledAttributes(attribute, R.styleable.BackdropLayout)

        // Front Layout
        frontLayoutId = typedArray.getResourceId(R.styleable.BackdropLayout_front_layout, 0)
        if (frontLayoutId == 0) {
            throw Exception("Must specify a front layout")
        }

        // Back Layout Container
        backLayoutContainerId = typedArray.getResourceId(R.styleable.BackdropLayout_back_layout_container, 0)
        if (backLayoutContainerId == 0) {
            throw Exception("Must specify a back layout container in form of Frame Layout")
        }

        // Backdrop Toolbar
        backdropToolbarId = typedArray.getResourceId(R.styleable.BackdropLayout_toolbarId,0)
        if (backdropToolbarId == 0) {
            throw Exception("Must reference the backdrop toolbar")
        }

        disableViewDrawableBuilder = DrawableBuilder().rectangle().apply {
            cornerRadii(
                dpToPx(frontLayoutCornerRadiusDp, context),
                dpToPx(frontLayoutCornerRadiusDp, context),
                0,
                0
            )
            solidColor(Color.BLACK)
        }

        typedArray.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        frontLayoutContainer = rootView.findViewById(frontLayoutId)
        backLayoutContainer = rootView.findViewById(backLayoutContainerId)
        backdropToolbar = rootView.findViewById(backdropToolbarId)
        frontViewInBackLayout = null

        bindBackLayoutToBackLayoutTriggers()
        insertFoldableActionsToBackLayoutTriggers()
    }

    private fun bindBackLayoutToBackLayoutTriggers() {
        val backLayoutChildren = mutableListOf<View>()
        for (i in 0 until backLayoutContainer.childCount) {
            backLayoutChildren.add(backLayoutContainer.getChildAt(i))
        }

        for (backdropViewLayoutBind in backdropToolbar.viewLayoutBindList) {
            for (child in backLayoutChildren) {
                if (child.sourceLayoutResId == backdropViewLayoutBind.boundLayoutId) {
                    backdropViewLayoutBind.populatedChild = child
                }
            }
        }
    }

    private fun insertFoldableActionsToBackLayoutTriggers() {
        backdropToolbar.viewLayoutBindList.forEach { button ->
            button.setOnClickListener {
                // When no back layout is shown
                if (frontViewInBackLayout == null) {
                    showBackLayout(button.populatedChild!!)
                }
                else {
                    // When the shown back layout is pressed again, close it
                    if (frontViewInBackLayout == button.populatedChild!!) {
                        collapseFrontLayout()
                    }
                    else {
                        // When the shown back layout is not pressed again, close it first then show the next one
                        switchBackLayout(button.populatedChild!!)
                    }
                }
            }
        }
    }

    private fun showBackLayout(backLayout: View) {
        startTranslateAnimation(
            endValue = backLayout.height.toFloat(),
            animationStartAction = {
                drawOverlayOnFrontLayout()
            }
        )
        backLayout.bringToFront()
        frontViewInBackLayout = backLayout
    }

    private fun switchBackLayout(backLayout: View) {
        startTranslateAnimation(
            endValue = 0F,
            animationEndAction = {
                startTranslateAnimation(
                    endValue = backLayout.height.toFloat(),
                    animationStartAction = {
                        drawOverlayOnFrontLayout()
                        backLayout.bringToFront()
                    }
                )
            }
        )

        frontViewInBackLayout = backLayout
    }

    private fun setDisableViewDrawableBuilder(newDisableViewDrawableBuilder: DrawableBuilder) {
        disableViewDrawableBuilder = newDisableViewDrawableBuilder
    }

    private fun drawOverlayOnFrontLayout() {
        frontLayoutContainer.foreground = disableViewDrawableBuilder.build()
        frontLayoutContainer.foreground.alpha = 100
    }

    private fun removeOverlayFromFrontLayer() {
        frontLayoutContainer.foreground = null
    }

    private fun collapseFrontLayout() {
        startTranslateAnimation(
            endValue = 0F,
            animationStartAction = {
                removeOverlayFromFrontLayer()
            }
        )
        frontViewInBackLayout = null
    }

    private fun startTranslateAnimation (endValue: Float, animationStartAction: (() -> Unit)? = null, animationEndAction: (() -> Unit)? = null) {
        val animator =  ValueAnimator()
        animator.apply {
            setFloatValues(frontLayoutContainer.translationY, endValue)
            duration = 400
            addUpdateListener {
                if (animationStartAction != null) {
                    animationStartAction()
                }
                val translation = it.animatedValue as Float
                frontLayoutContainer.translationY = translation
            }
            doOnEnd {
                if (animationEndAction != null) {
                    animationEndAction()
                }
            }
            start()
        }
    }
}