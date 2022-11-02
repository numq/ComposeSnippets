package com.numq.composesnippets.components.reorderable.modifier

sealed class ListType private constructor() {
    object Row : ListType()
    object Column : ListType()
    object Grid : ListType()
}