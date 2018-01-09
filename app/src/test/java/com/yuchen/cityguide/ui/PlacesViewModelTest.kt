package com.yuchen.cityguide.ui

import android.app.Application
import com.yuchen.cityguide.data.*
import io.reactivex.Observable
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import io.reactivex.schedulers.TestScheduler
import org.mockito.Mockito.*


/**
 * Created by yuchen on 1/8/18.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PlacesViewModelTest {

    @Mock private lateinit var mPlacesRepository: PlacesRepository
    @Mock private lateinit var mContext: Application
    @Mock private lateinit var mLocation: PlacesRepository.PlaceLocation

    private lateinit var mPlacesViewModel: PlacesViewModel
    private val mPlaceList = mutableListOf<Place>()
    private val mScheduler = TestScheduler()


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        mPlacesViewModel = PlacesViewModel(mContext, mPlacesRepository, mScheduler, mScheduler)

        val place1 = Place(name = "The Creamery", type = PlaceType.CAFE, rating = 4f, geometry =
        Geometry(Location(37.7774628, 122.3951578)))
        val place2 = Place(name = "La Mar Cebicheria Peruana", type = PlaceType.BISTRO, rating = 4.4f,
                geometry = Geometry(Location(37.797387, -122.395196)))
        val place3 = Place(name = "Clift San Francisco", type = PlaceType.BAR, rating = 4.1f,
                geometry =
                Geometry(Location(37.7867167, -122.4111737)))
        val place4 = Place(name = "Comstock Saloon", type = PlaceType.BAR, rating = 4.5f,
                geometry =
                Geometry(Location(37.79692980000001, -122.4055622)))

        mPlaceList.add(place1)
        mPlaceList.add(place2)
        mPlaceList.add(place3)
        mPlaceList.add(place4)

        doReturn(Observable.just(mPlaceList)).`when`(mPlacesRepository).getPlaces(any())
    }

    @Test
    fun testLoadPlacesTypeBar() {

        mPlacesViewModel.currentFiltering = PlaceType.BAR
        mPlacesViewModel.loadPlaces(true, true, mLocation)
        mScheduler.triggerActions();

        with(mPlacesViewModel) {
            assertTrue(!dataLoading.get())
            assertTrue(items.size == 2)
            items.forEach {
                assertTrue(it.name == "Comstock Saloon" || it.name == "Clift San Francisco")

            }

        }
    }

    @Test
    fun testLoadPlacesTypeCafe() {

        mPlacesViewModel.currentFiltering = PlaceType.CAFE
        mPlacesViewModel.loadPlaces(true, true, mLocation)
        mScheduler.triggerActions();

        with(mPlacesViewModel) {
            assertTrue(!dataLoading.get())
            assertTrue(items.size == 1)
            assertTrue(items[0].name == "The Creamery")
        }
    }

    @Test
    fun testLoadPlacesTypeBistro() {

        mPlacesViewModel.currentFiltering = PlaceType.BISTRO
        mPlacesViewModel.loadPlaces(true, true, mLocation)
        mScheduler.triggerActions();

        with(mPlacesViewModel) {
            assertTrue(!dataLoading.get())
            assertTrue(items.size == 1)
            assertTrue(items[0].name == "La Mar Cebicheria Peruana")

        }
    }


    private fun <T> any(): T {
        Mockito.any<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T
}