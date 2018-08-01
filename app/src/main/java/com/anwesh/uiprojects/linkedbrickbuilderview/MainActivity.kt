package com.anwesh.uiprojects.linkedbrickbuilderview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.brickbuilderview.BrickBuilderView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BrickBuilderView.create(this)
    }
}
