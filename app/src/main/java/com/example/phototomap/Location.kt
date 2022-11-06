package com.example.phototomap
data class Location(
    var id: Int,
    var longitude: Double,
    var latitude: Double,
    var image: String
) {
    constructor(longitude: Double, latitude: Double, image: String):
            this (-1, longitude, latitude, image)
}
