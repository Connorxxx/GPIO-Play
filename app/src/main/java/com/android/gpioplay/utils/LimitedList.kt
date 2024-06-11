package com.android.gpioplay.utils

class LimitedList<T>(private val list: ArrayList<T>) : MutableList <T> by list {

    companion object {
        private const val LIMIT = 2
    }

  override fun add(element: T): Boolean {
      if (size >= LIMIT) list.removeAt(0)
      return list.add(element)
  }

}