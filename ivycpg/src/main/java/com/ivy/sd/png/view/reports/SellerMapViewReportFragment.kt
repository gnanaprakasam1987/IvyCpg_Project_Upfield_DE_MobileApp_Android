package com.ivy.sd.png.view.reports

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatDrawableManager
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ui.IconGenerator
import com.ivy.maplib.MapWrapperLayout
import com.ivy.sd.png.asean.view.R
import com.ivy.sd.png.bo.OutletReportBO
import com.ivy.sd.png.model.BusinessModel
import com.ivy.sd.png.provider.ConfigurationMasterHelper
import com.ivy.sd.png.util.Commons
import com.ivy.sd.png.view.HomeScreenActivity
import com.ivy.sd.png.view.SellerListFragment
import com.ivy.sd.png.view.profile.DirectionsJSONParser
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Created by rajkumar.s on 10/27/2017.
 *
 */

class SellerMapViewReportFragment : SupportMapFragment(), SellerListFragment.SellerSelectionInterface {

    internal lateinit var view: View
    internal lateinit var bmodel: BusinessModel

    private lateinit var mDrawerLayout: DrawerLayout
    internal lateinit var drawer: FrameLayout

    private var lstReports: ArrayList<OutletReportBO>? = null

    private var mMap: GoogleMap? = null
    private var mapView: View? = null
    internal lateinit var mainLayout: MapWrapperLayout
    private var layInflater: LayoutInflater? = null

    internal lateinit var infoWindow: ViewGroup
    internal lateinit var infoTitle: TextView
    internal lateinit var infoLocName: TextView
    internal lateinit var infoAddress: TextView
    internal lateinit var infoTimeIn: TextView
    internal lateinit var infoTimeOut: TextView
    internal lateinit var infoSalesValue: TextView
    internal lateinit var infoSeller: TextView
    internal lateinit var iv_planned: ImageView
    internal lateinit var iv_deviated: ImageView
    private var markerList: MutableList<MarkerOptions>? = null
    private var bounds: LatLngBounds? = null
    private var builder: LatLngBounds.Builder? = null

    private val actionBar: ActionBar?
        get() = (activity as AppCompatActivity).supportActionBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mapView = super.onCreateView(inflater, container, savedInstanceState)

        try {
            activity.window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

            view = inflater.inflate(R.layout.fragment_seller_mapview, container, false)

            layInflater = inflater

            bmodel = activity.applicationContext as BusinessModel
            bmodel.setContext(activity)

            if (bmodel.userMasterHelper.userMasterBO.userid == 0) {
                Toast.makeText(activity,
                        resources.getString(R.string.sessionout_loginagain),
                        Toast.LENGTH_SHORT).show()
                activity.finish()
            }


            if (lstReports == null)
                lstReports = bmodel.reportHelper.downloadOutletReports()


        } catch (ex: Exception) {
            Commons.printException(ex)
        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainLayout = view
                .findViewById(R.id.planningmapnew) as MapWrapperLayout
        mainLayout.addView(mapView)

    }

    override fun onStart() {
        super.onStart()

        try {
            setHasOptionsMenu(true)
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            actionBar!!.setDisplayShowHomeEnabled(true)
            actionBar!!.setHomeButtonEnabled(true)

            val mDrawerToggle: ActionBarDrawerToggle
            drawer = getView()!!.findViewById(R.id.right_drawer) as FrameLayout

            mDrawerLayout = getView()!!.findViewById(
                    R.id.drawer_layout) as DrawerLayout

            mDrawerLayout!!.setDrawerShadow(R.drawable.drawer_shadow,
                    GravityCompat.START)
            mDrawerLayout!!.setDrawerShadow(R.drawable.drawer_shadow,
                    GravityCompat.END)
            mDrawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            mDrawerToggle = object : ActionBarDrawerToggle(activity,
                    mDrawerLayout,
                    R.string.ok,
                    R.string.close
            ) {
                override fun onDrawerClosed(view: View) {
                    /* if (getActionBar() != null) {
                        ((TextView)getActivity(). findViewById(R.id.tv_toolbar_title)).setText(bmodel.mSelectedActivityName);
                    }
*/
                    activity.supportInvalidateOptionsMenu()
                }

                override fun onDrawerOpened(drawerView: View) {
                    if (actionBar != null) {
                        (activity.findViewById(R.id.tv_toolbar_title) as TextView).text = resources.getString(R.string.filter)
                    }

                    activity.supportInvalidateOptionsMenu()
                }
            }

            mDrawerLayout!!.addDrawerListener(mDrawerToggle)
            mDrawerLayout!!.closeDrawer(GravityCompat.END)

            initializeMap()

        } catch (ex: Exception) {
            Commons.printException(ex)
        }


    }

    private fun initializeMap() {

        mMap = this.map
        mMap!!.uiSettings.isMapToolbarEnabled = false
        mMap!!.uiSettings.isZoomControlsEnabled = false

        val pxlDp = (39 + 20).toFloat()
        mainLayout.init(mMap, getPixelsFromDp(activity, pxlDp))
        this.infoWindow = layInflater!!.inflate(
                R.layout.outlet_info_window, null) as ViewGroup
        this.infoTitle = infoWindow.findViewById(R.id.tv_storename) as TextView
        this.infoLocName = infoWindow.findViewById(R.id.tv_loc_name) as TextView
        this.infoAddress = infoWindow.findViewById(R.id.tv_address) as TextView
        this.infoTimeIn = infoWindow.findViewById(R.id.tv_time_in) as TextView
        this.infoTimeOut = infoWindow.findViewById(R.id.tv_time_out) as TextView
        this.infoSalesValue = infoWindow.findViewById(R.id.tv_sales_value) as TextView
        this.iv_planned = infoWindow.findViewById(R.id.iv_planned) as ImageView
        this.iv_deviated = infoWindow.findViewById(R.id.iv_deviate) as ImageView
        this.infoSeller = infoWindow.findViewById(R.id.tv_seller) as TextView

        this.infoTitle.typeface = bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM)
        this.infoLocName.typeface = bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM)
        this.infoAddress.typeface = bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM)
        this.infoTimeIn.typeface = bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM)
        this.infoTimeOut.typeface = bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM)
        this.infoSalesValue.typeface = bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM)
        this.infoSeller.typeface = bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM)

        (infoWindow.findViewById(R.id.lbl_time_in) as TextView).typeface = bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT)
        (infoWindow.findViewById(R.id.lbl_time_out) as TextView).typeface = bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT)
        (infoWindow.findViewById(R.id.lbl_sales_value) as TextView).typeface = bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT)

        mMap!!.uiSettings.isZoomControlsEnabled = true
        mMap!!.isMyLocationEnabled = false
        mMap!!.setInfoWindowAdapter(CustomInfoWindowAdapter())

        mMap!!.setOnMapLoadedCallback { updateUserSelection(null, true) }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_seller_mapview, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val drawerOpen = mDrawerLayout!!.isDrawerOpen(GravityCompat.END)
        menu.findItem(R.id.menu_users).isVisible = !drawerOpen
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.menu_users) {
            loadUsers()
        } else if (i == android.R.id.home) {
            if (mDrawerLayout!!.isDrawerOpen(GravityCompat.END))
                mDrawerLayout!!.closeDrawers()
            else {
                onBackButtonClick()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onBackButtonClick() {
        val i = Intent(activity, HomeScreenActivity::class.java)
        i.putExtra("menuCode", "MENU_REPORT")
        i.putExtra("title", "aaa")
        startActivity(i)
        activity.finish()
        activity.overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out)

    }

    private fun loadUsers() {

        try {
            mDrawerLayout!!.openDrawer(GravityCompat.END)

            val fm = activity.supportFragmentManager
            val frag: SellerListFragment?
            frag = fm.findFragmentByTag("filter") as? SellerListFragment
            val ft = fm.beginTransaction()
            if (frag != null)
                ft.detach(frag)

            val fragment = SellerListFragment()
            val bundle = Bundle()
            bundle.putParcelableArrayList("users", bmodel.reportHelper.lstUsers)
            fragment.setArguments(bundle)

            ft.replace(R.id.right_drawer, fragment, "filter")
            ft.commit()

            mDrawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
        } catch (ex: Exception) {
            Commons.printException(ex)
        }

    }

    override fun updateUserSelection(mSelectedUsers: ArrayList<Int>?, isAlluser: Boolean) {
        try {

            if (lstReports != null) {

                var storeLatLng: LatLng
                markerList = ArrayList()

                if (mMap != null) {
                    mMap!!.clear()
                }
                mDrawerLayout!!.closeDrawers()

                //If nothing selected then showing default text
                if (actionBar != null) {
                    (activity.findViewById(R.id.tv_toolbar_title) as TextView).text = bmodel.mSelectedActivityName
                }

                var lstLastVisitedRetailerIds: ArrayList<Int>? = null
                if (isAlluser) {
                    lstLastVisitedRetailerIds = ArrayList()
                    for (userBo in bmodel.reportHelper.lstUsers) {
                        lstLastVisitedRetailerIds.add(bmodel.reportHelper.downloadlastVisitedRetailer(userBo.userId))
                    }

                }

                // get total retailers for selected user to show end marker
                var totalRetailers = 0
                if (!isAlluser) {
                    for (bo in lstReports!!) {
                        if (mSelectedUsers != null && mSelectedUsers.contains(bo.userId)) {
                            totalRetailers += 1
                        }
                    }
                }

                var sequence = 0
                for (bo in lstReports!!) {
                    if (isAlluser && lstLastVisitedRetailerIds!!.contains(bo.retailerId) || mSelectedUsers != null && mSelectedUsers.contains(bo.userId)) {

                        if (isValidLatLng(bo.latitude, bo.longitude)) {
                            if (bo.latitude != 0.0 && bo.longitude != 0.0) {

                                if (!isAlluser) {
                                    sequence += 1
                                    bo.sequence = sequence

                                    //update screen title
                                    if (actionBar != null) {
                                        (activity.findViewById(R.id.tv_toolbar_title) as TextView).text = bo.userName
                                    }

                                } else {
                                    bo.sequence = 0

                                    //update screen title
                                    if (actionBar != null) {
                                        (activity.findViewById(R.id.tv_toolbar_title) as TextView).text = bmodel.mSelectedActivityName
                                    }

                                }

                                storeLatLng = LatLng(bo.latitude, bo.longitude)

                                val iconFactory = IconGenerator(activity)
                                val markerOptions = MarkerOptions().title(bo.retailerName).position(storeLatLng).anchor(iconFactory.anchorU, iconFactory.anchorV)
                                        .snippet(bo.retailerId.toString() + "")

                                if (bo.sequence == 1) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("S")))
                                } else if (bo.sequence != 0 && bo.sequence == totalRetailers) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("E")))
                                } else {
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(activity, R.drawable.ic_marker_person)))
                                }
                                markerList!!.add(markerOptions)

                                map.addMarker(markerOptions)


                            }
                        }
                    }
                }


                //fit all markers into the screen
                if (markerList!!.size > 0) {
                    if (builder == null) {
                        builder = LatLngBounds.Builder()
                    } else {
                        builder = null
                        builder = LatLngBounds.Builder()
                    }

                    for (i in markerList!!.indices) {
                        val latLng = markerList!![i].position
                        val lat = latLng.latitude
                        val lng = latLng.longitude
                        if (lat != 0.0 && lng != 0.0) {
                            builder!!.include(markerList!![i].position)

                        }
                    }
                    if (bounds == null) {
                        bounds = builder!!.build()
                    } else {
                        bounds = null
                        bounds = builder!!.build()
                    }

                    if (bounds != null)
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,
                                100))
                }

                if (!isAlluser) {
                    drawMapRoute()
                }

            }


        } catch (ex: Exception) {
            Commons.printException(ex)
        }


    }


    private inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

        override fun getInfoContents(marker: Marker): View? {
            return null
        }

        override fun getInfoWindow(marker: Marker): View {

            try {
                infoTitle.text = marker.title
                val retailerd = Integer.parseInt(marker.snippet)
                for (bo in lstReports!!) {
                    if (bo.retailerId == retailerd) {
                        infoLocName.text = bo.locationName
                        infoAddress.text = bo.address
                        infoTimeIn.text = bo.timeIn
                        infoTimeOut.text = bo.timeOut
                        infoSalesValue.text = bo.salesValue
                        infoSeller.text = "Seller: " + bo.userName

                        if (bo.isPlanned == 1) {
                            iv_planned.visibility = View.VISIBLE
                            iv_deviated.visibility = View.GONE
                        } else if (bo.isPlanned == 0 && bo.isVisited == 1) {
                            iv_deviated.visibility = View.VISIBLE
                            iv_planned.visibility = View.GONE
                        } else {
                            iv_deviated.visibility = View.GONE
                            iv_planned.visibility = View.GONE
                        }

                        break
                    }
                }
            } catch (ex: Exception) {
                Commons.printException(ex)
            }

            mainLayout.setMarkerWithInfoWindow(marker, infoWindow)
            return infoWindow
        }

    }// CustomInfoWindowAdapter

    /**
     * Method used to validate lat long values.
     *
     * @param lat
     * @param lng
     * @return
     */
    fun isValidLatLng(lat: Double, lng: Double): Boolean {
        if (lat < -90 || lat > 90) {
            return false
        } else if (lng < -180 || lng > 180) {
            return false
        }
        return true
    }

    override fun updateClose() {
        mDrawerLayout!!.closeDrawers()

        if (actionBar != null) {
            (activity.findViewById(R.id.tv_toolbar_title) as TextView).text = bmodel.mSelectedActivityName
        }
    }


    private fun drawMapRoute() {
        try {
            if (markerList!!.size >= 2) {
                val origin = markerList!![0].position
                val dest = markerList!![markerList!!.size - 1].position

                // Getting URL to the Google Directions API
                val url = getDirectionsUrl(origin, dest)
                val downloadTask = DownloadTask()
                downloadTask.execute(url)


            }
        } catch (ex: Exception) {
            Commons.printException(ex)
        }

    }

    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {

        val str_origin = "origin=" + origin.latitude + "," + origin.longitude
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude // Destination of route
        val sensor = "sensor=false"
        var waypoints = ""

        for (i in 2 until markerList!!.size) {
            val point = markerList!![i].position
            if (i == 2)
                waypoints = "waypoints="
            waypoints += point.latitude.toString() + "," + point.longitude + "|"
        }

        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor&$waypoints"
        val output = "json"

        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
    }

    // Fetches data from url passed
    private inner class DownloadTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg url: String): String {

            var data = ""
            try {
                data = downloadUrl(url[0])
            } catch (e: Exception) {
                Commons.printException("Background Task", e)
            }

            return data
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            val parserTask = ParserTask()
            parserTask.execute(result)
        }
    }

    /**
     * A method to download json data from url
     */
    @SuppressLint("LongLogTag")
    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        val iStream: InputStream
        var urlConnection: HttpURLConnection
        try {
            // Creating an http connection to communicate with url
            val url = URL(strUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()
            iStream = urlConnection.inputStream

            // val br = BufferedReader(InputStreamReader(iStream))

            data = iStream.bufferedReader().readText()

            //  br.close()
            iStream.close()
            urlConnection.disconnect()
        } catch (e: Exception) {
            Commons.printException("Exception while downloading url", e)
        }

        return data
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private inner class ParserTask : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {

        // Parsing the data in non-ui thread
        override fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>>? {

            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null
            try {
                jObject = JSONObject(jsonData[0])
                val parser = DirectionsJSONParser()
                routes = parser.parse(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return routes
        }

        // Executes in UI thread, after the parsing process
        override fun onPostExecute(result: List<List<HashMap<String, String>>>) {

            var points: ArrayList<LatLng>
            var lineOptions: PolylineOptions? = null

            // Traversing through all the routes
            for (i in result.indices) {
                points = ArrayList()
                lineOptions = PolylineOptions()

                // Fetching i-th route
                val path = result[i]
                // Fetching all the points in i-th route
                for (j in path.indices) {
                    val point = path[j]

                    val lat = java.lang.Double.parseDouble(point["lat"])
                    val lng = java.lang.Double.parseDouble(point["lng"])
                    val position = LatLng(lat, lng)

                    points.add(position)
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points)
                lineOptions.width(5f)
                lineOptions.color(Color.RED)
            }
            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap!!.addPolyline(lineOptions)
            }
        }
    }

    companion object {

        fun getPixelsFromDp(context: Context, dp: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dp * scale + 0.5f).toInt()
        }

        fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
            var drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = DrawableCompat.wrap(drawable).mutate()
            }

            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth,
                    drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }
    }
}
