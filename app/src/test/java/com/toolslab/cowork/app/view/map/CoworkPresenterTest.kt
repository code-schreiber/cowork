package com.toolslab.cowork.app.view.map

import com.nhaarman.mockito_kotlin.*
import com.toolslab.cowork.BuildConfig
import com.toolslab.cowork.app.util.max
import com.toolslab.cowork.app.util.min
import com.toolslab.cowork.base_network.storage.Credentials
import com.toolslab.cowork.base_repository.exception.NoConnectionException
import com.toolslab.cowork.base_repository.exception.NotFoundException
import com.toolslab.cowork.base_repository.model.Space
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class CoworkPresenterTest {

    private val errorMessage = "an error message"
    private val error = Exception(errorMessage)
    private val country = "a country"
    private val city = "a city"
    private val space = "a space"
    private val latitude1 = 1.1
    private val latitude2 = 2.2
    private val longitude1 = 1.13
    private val longitude2 = 2.24
    private val credentials = Credentials(BuildConfig.API_USER, BuildConfig.API_PASSWORD)
    private val minLatitude = listOf(latitude1, latitude2).min()
    private val minLongitude = listOf(longitude1, longitude2).min()
    private val maxLatitude = listOf(latitude1, latitude2).max()
    private val maxLongitude = listOf(longitude1, longitude2).max()
    private val space1 = Space("space1", "snippet1", latitude1, longitude1)

    private val space2 = Space("space2", "snippet2", latitude2, longitude2)
    private val spaces = listOf(space1, space2)

    private val mockCompositeDisposable: CompositeDisposable = mock()
    private val mockCoworkInteractor: CoworkInteractor = mock()
    private val mockView: CoworkContract.View = mock()

    private val underTest = CoworkPresenter()

    companion object {

        @BeforeClass
        @JvmStatic
        fun setUpRxSchedulers() {
            val immediate = object : Scheduler() {

                // this prevents StackOverflowErrors when scheduling with a delay
                override fun scheduleDirect(run: Runnable, delay: Long, unit: TimeUnit) = super.scheduleDirect(run, 0, unit)

                override fun createWorker() = ExecutorScheduler.ExecutorWorker(Executor { it.run() })
            }

            RxAndroidPlugins.setInitMainThreadSchedulerHandler { immediate }
            RxJavaPlugins.setIoSchedulerHandler { immediate }
        }
    }

    @Before
    fun setUp() {
        underTest.compositeDisposable = mockCompositeDisposable
        underTest.coworkInteractor = mockCoworkInteractor
        underTest.bind(mockView)
    }

    @Test
    fun onBound() {
        verify(mockView).getMapAsync()
    }

    @Test
    fun onUnbound() {
        underTest.onUnbound(mockView)

        verify(mockCompositeDisposable).clear()
    }

    @Test
    fun searchSpaces() {
        whenever(mockCoworkInteractor.listSpaces(credentials, country, city, space)).thenReturn(Single.just(spaces))

        underTest.searchSpaces(country, city, space)

        verify(mockView).addMapMarker(space1)
        verify(mockView).addMapMarker(space2)
        verify(mockView).moveCamera(minLatitude, minLongitude, maxLatitude, maxLongitude)
        verify(mockCompositeDisposable).add(any())
    }

    @Test
    fun searchSpacesWithNoPlacesFoundError() {
        whenever(mockCoworkInteractor.listSpaces(credentials, country, city, space)).thenReturn(Single.error(NotFoundException(error)))

        underTest.searchSpaces(country, city, space)

        verify(mockView).getMapAsync()
        verify(mockView).showNoPlacesFoundError("$city, $country")
        verify(mockCompositeDisposable).add(any())
        verifyNoMoreInteractions(mockView)
    }

    @Test
    fun searchSpacesWithNoConnectionError() {
        whenever(mockCoworkInteractor.listSpaces(credentials, country, city, space)).thenReturn(Single.error(NoConnectionException(error)))

        underTest.searchSpaces(country, city, space)

        verify(mockView).getMapAsync()
        verify(mockView).showNoConnectionError()
        verify(mockCompositeDisposable).add(any())
        verifyNoMoreInteractions(mockView)
    }

    @Test
    fun searchSpacesWithDefaultError() {
        whenever(mockCoworkInteractor.listSpaces(credentials, country, city, space)).thenReturn(Single.error(error))

        underTest.searchSpaces(country, city, space)

        verify(mockView).getMapAsync()
        verify(mockView).showDefaultError()
        verify(mockCompositeDisposable).add(any())
        verifyNoMoreInteractions(mockView)
    }

    @Test
    fun searchSpacesWithoutCountry() {
        underTest.searchSpaces("", "", "")

        verify(mockView).getMapAsync()
        verify(mockView).showInputMissesCountryError()
        verifyNoMoreInteractions(mockView)
        verifyZeroInteractions(mockCoworkInteractor)
        verifyZeroInteractions(mockCompositeDisposable)
    }

    @Test
    fun onMapReady() {
        underTest.onMapReady()

        verify(mockView).showSearch()
    }

    @Test
    fun onUserMapGestureStopped() {
        whenever(mockCoworkInteractor.listSpaces(credentials, country, city)).thenReturn(Single.just(spaces))

        underTest.onUserMapGestureStopped(country, city)

        verify(mockView).getMapAsync()
        verify(mockView).removeMarkers()
        verify(mockView).addMapMarker(space1)
        verify(mockView).addMapMarker(space2)
        verify(mockCompositeDisposable).clear()
        verify(mockCompositeDisposable).add(any())
    }

    @Test
    fun onUserMapGestureStoppedWithSameLocation() {
        whenever(mockCoworkInteractor.listSpaces(credentials, country, city)).thenReturn(Single.just(spaces))

        underTest.onUserMapGestureStopped(country, city)

        verify(mockView).getMapAsync()
        verify(mockView).removeMarkers()
        verify(mockView).addMapMarker(space1)
        verify(mockView).addMapMarker(space2)
        verify(mockCompositeDisposable).clear()
        verify(mockCompositeDisposable).add(any())

        underTest.onUserMapGestureStopped(country, city)

        verifyNoMoreInteractions(mockView)
        verifyNoMoreInteractions(mockCompositeDisposable)
    }

    @Test
    fun moveCamera() {
        underTest.moveCamera(spaces)

        verify(mockView).moveCamera(minLatitude, minLongitude, maxLatitude, maxLongitude)
    }

}
