package com.example.phototomap
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private val DATABASE_NAME="db"
        private val DATABASE_VERSION=1
        private val TABLE_NAME="locations"
        private val ID="id"
        private val LONGITUDE="longitude"
        private val LATITUDE="latitude"
        private val IMAGE="image"
    }
    override fun onCreate(p0: SQLiteDatabase?) {
        val query = "CREATE TABLE $TABLE_NAME ($ID INTEGER PRIMARY KEY, $LONGITUDE DECIMAL, $LATITUDE DECIMAL, $IMAGE INTEGER)"
        p0?.execSQL(query)
    }
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(p0)
    }
    fun insertLocation (location: Location) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(LONGITUDE, location.longitude)
        contentValues.put(LATITUDE, location.latitude)
        contentValues.put(IMAGE, location.image)
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }
    fun getLocationList (): ArrayList<Location> {
        var locationList: ArrayList<Location> = ArrayList()
        var query = "SELECT * FROM $TABLE_NAME"
        var db = this.readableDatabase
        val result = db.rawQuery(query,null)
        if (result.moveToFirst()) {
            do {
                val id = result.getInt(result.getColumnIndex(ID).toInt())
                val longitude = result.getDouble(result.getColumnIndex(LONGITUDE).toInt())
                val latitude = result.getDouble(result.getColumnIndex(LATITUDE).toInt())
                val image = result.getString(result.getColumnIndex(IMAGE).toInt())
                locationList.add(Location(id, longitude, latitude, image))
            } while (result.moveToNext())
        }
        db.close()
        return locationList
    }
    fun deleteLocationList () {
        val db = this.writableDatabase
        var query = "DELETE FROM $TABLE_NAME"
        db.execSQL(query)
    }
}