package com.yuchen.cityguide.data

/**
 * Created by yuchen on 1/7/18.
 */
data class DistanceResponse (var rows: List<Row>)

data class Row(var elements: List<Element>)

data class Element(var distance: Distance)

data class Distance(var text: String, var value: Int)

