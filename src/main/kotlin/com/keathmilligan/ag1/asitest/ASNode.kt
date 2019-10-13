package com.keathmilligan.ag1.asitest

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Array

class ASNode(val map : ASNodeMap, val x : Int, val y : Int, var isOpen : Boolean) {
    val index = y * map.height + x
    val connections = Array<Connection<ASNode>>()
    val rect = Rectangle()
}
