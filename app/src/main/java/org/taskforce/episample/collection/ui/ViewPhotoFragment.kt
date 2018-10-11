package org.taskforce.episample.collection.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_view_photo.*
import org.taskforce.episample.R
import org.taskforce.episample.utils.loadImage

class ViewPhotoFragment: DialogFragment() {
    
    lateinit var imageUri: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        imageUri = arguments!!.getString(PHOTO_URI_KEY)
        
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewPhoto.loadImage(imageUri, null)
        
        closeButton.setOnClickListener { 
            dismiss()
        }
    }

    companion object {
        const val PHOTO_URI_KEY = "PhotoUriKey"
        
        fun newInstance(imageUri: String): DialogFragment {
            val fragment = ViewPhotoFragment()
            fragment.arguments = Bundle().apply { 
                putString(PHOTO_URI_KEY, imageUri)
            }
            return fragment
        }
    }
}