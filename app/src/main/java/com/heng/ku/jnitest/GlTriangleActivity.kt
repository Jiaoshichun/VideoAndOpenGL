package com.heng.ku.jnitest

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.heng.ku.jnitest.opengl.SimpleRender
import com.heng.ku.jnitest.opengl.drawer.*
import kotlinx.android.synthetic.main.activity_gl_triangle.*

class GlTriangleActivity : AppCompatActivity() {
    private var drawer: IDrawer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gl_triangle)
        val type = intent.getIntExtra("type", 0)
        drawer = if (type == Type.BITMAP.ordinal) {
            BitmapDrawer(BitmapFactory.decodeResource(resources, R.drawable.cover))
        } else {
            Type.values().find {
                it.ordinal == type
            }!!.clazz.newInstance()
        }
        glSurfaceView.setEGLContextClientVersion(2)
        val render = SimpleRender()
        render.addDrawer(drawer!!)
        glSurfaceView.setRenderer(render)
    }

    override fun onDestroy() {
        super.onDestroy()
        drawer?.release()
    }

    companion object {
        fun start(context: Activity, type: Type) {
            context.startActivity(Intent(context, GlTriangleActivity::class.java).putExtra("type", type.ordinal))
        }
    }

    enum class Type(val clazz: Class<out IDrawer>) {
        TRIANGLE(TriangleDrawer::class.java),
        COLOR_TRIANGLE(IsoscelesTriangleDrawer::class.java),
        CUBE(CubeDrawer::class.java),
        BITMAP(BitmapDrawer::class.java),
        CIRCLE(CircleDrawer::class.java),
        CONE(ConeDrawer::class.java),
        CYLINDER(CylinderDrawer::class.java),
    }
}