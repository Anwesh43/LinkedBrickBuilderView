package com.anwesh.uiprojects.brickbuilderview

/**
 * Created by anweshmishra on 02/08/18.
 */
import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.content.Context
import android.graphics.Color
import android.graphics.RectF

fun Canvas.drawBBNode(i : Int, scale : Float, cb : (Canvas) -> Unit, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = Color.parseColor("#f44336")
    val yGap : Float = (0.4f * h) / nodes
    val xGap : Float = w / nodes
    val wSize : Float = xGap/3
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    val y : Float = yGap + (h - yGap) * (1 - sc1)
    save()
    translate(xGap * sc2, 0f)
    cb(this)
    save()
    translate(xGap * i + xGap / 2, h/2)
    for(j in 0..1) {
        save()
        scale(1f, 1f - 2 * j)
        drawRect(RectF(-wSize / 2, y, wSize / 2, y + yGap), paint)
        restore()
    }
    restore()
    restore()
}

val nodes : Int = 5

class BrickBuilderView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.invalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BBNode(var i : Int, val state : State = State()) {

        private var next : BBNode? = null

        private var prev : BBNode? = null

        init {
            addNeighbor()
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBBNode(i, state.scale, {
                next?.draw(it, paint)
            }, paint)
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BBNode(i + 1)
                next?.prev = this
            }
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BBNode {
            var curr : BBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedBrickBuilder(var i : Int) {

        private var curr : BBNode = BBNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BrickBuilderView) {

        private val linkedBB : LinkedBrickBuilder = LinkedBrickBuilder(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            linkedBB.draw(canvas, paint)
            animator.animate {
                linkedBB.update {i, scale ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            linkedBB.startUpdating {
                animator.start()
            }
        }
    }
}