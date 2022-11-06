package com.example.phototomap
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BottomSheetFragment: BottomSheetDialogFragment() {
    private var location: String? = null
    private var image: String? = null
    lateinit var address_textview : TextView
    lateinit var imageview : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetTheme)
        arguments?.let {
            location = it.getString(ARG_PARAM1)
            image = it.getString(ARG_PARAM2)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.bottom_sheet_layout, container, false)
        address_textview = view.findViewById(R.id.address)
        imageview = view.findViewById(R.id.imageView2)
        address_textview.text = location
        // imageview.setImageResource()
        var bitmap: Bitmap = BitmapFactory.decodeFile(image)
        imageview.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 340, 300, false))
        return view
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String?, param2: String?) =
            BottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}