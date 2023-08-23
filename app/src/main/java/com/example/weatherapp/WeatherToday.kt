package com.example.weatherapp

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.weatherapp.adapter.WeatherToday
import com.example.weatherapp.databinding.TestlayoutBinding
import com.example.weatherapp.mvvm.WeatherVm
import java.text.SimpleDateFormat
import java.util.*


@SuppressWarnings("DEPRECATION")
class WeatherToday : Fragment() {

    lateinit var viM: WeatherVm

    lateinit var adapter: WeatherToday

    private lateinit var binding: TestlayoutBinding


    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //inflate the fragment layout
        binding = DataBindingUtil.inflate(inflater, R.layout.testlayout, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //viewmodel
        viM = ViewModelProvider(this).get(WeatherVm::class.java)
        viM.getWeather()
        adapter = WeatherToday()

        //clear city
        val sharedPrefs = Preferences.getInstance(requireActivity())
        sharedPrefs.clearCityValue()



        //observe LiveData za danasnje vrijeme
        viM.todayWeatherLiveData.observe(viewLifecycleOwner, Observer {
            val setNewlist = it as List<WeatherList>
            adapter.setList(setNewlist)
            binding.forecastRecyclerView.adapter = adapter
        })

        //bind ViewModel i data na layout
        binding.lifecycleOwner = this
        binding.vm = viM

        Glide.with(this)
            .load(R.drawable.rain)
            .into(binding.mainRainIcon)

        Glide.with(this)
            .load(R.drawable.wind)
            .into(binding.mainWindIcon)

        Glide.with(this)
            .load(R.drawable.humidity)
            .into(binding.mainHumidityIcon)

        viM.closetorexactlysameweatherdata.observe(viewLifecycleOwner, Observer {
            val temperatureFahrenheit = it!!.main?.temp
            val temperatureCelsius = temperatureFahrenheit?.minus(273.15)
            val temperatureFormatted = String.format("%.2f", temperatureCelsius)

            //update UI
            for (i in it.weather) {
                binding.descMain.text = i.description
            }

            binding.tempMain.text = "$temperatureFormatted°"

            binding.humidityMain.text = it.main!!.humidity.toString()
            binding.windSpeed.text = it.wind?.speed.toString()


            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = inputFormat.parse(it.dtTxt!!)
            val outputFormat = SimpleDateFormat("d MMMM EEEE", Locale.getDefault())
            val dateanddayname = outputFormat.format(date!!)

            binding.dateDayMain.text = dateanddayname

            binding.chanceofrain.text = "${it.pop.toString()}%"

            //postavljanje ikona prema vremenu - glide
            for (i in it.weather) {
                if (i.icon == "01d") {
                    Glide.with(this)
                        .load(R.drawable.sun)
                        .into(binding.imageMain)
                }
                if (i.icon == "01n") {
                    Glide.with(this)
                        .load(R.drawable.clear)
                        .into(binding.imageMain)
                }
                if (i.icon == "02d") {
                    Glide.with(this)
                        .load(R.drawable.psun)
                        .into(binding.imageMain)
                }
                if (i.icon == "02n") {
                    Glide.with(this)
                        .load(R.drawable.pnight)
                        .into(binding.imageMain)
                }
                if (i.icon == "03d" || i.icon == "03n") {
                    Glide.with(this)
                        .load(R.drawable.cloud1)
                        .into(binding.imageMain)
                }
                if (i.icon == "10d") {
                    Glide.with(this)
                        .load(R.drawable.sunrain)
                        .into(binding.imageMain)
                }
                if (i.icon == "10n") {
                    Glide.with(this)
                        .load(R.drawable.nrain)
                        .into(binding.imageMain)
                }
                if (i.icon == "04d" || i.icon == "04n") {
                    Glide.with(this)
                        .load(R.drawable.cloudss)
                        .into(binding.imageMain)
                }
                if (i.icon == "09d" || i.icon == "09n") {
                    Glide.with(this)
                        .load(R.drawable.drops)
                        .into(binding.imageMain)
                }
                if (i.icon == "11d" || i.icon == "11n") {
                    Glide.with(this)
                        .load(R.drawable.thunder)
                        .into(binding.imageMain)
                }
                if (i.icon == "13d" || i.icon == "13n") {
                    Glide.with(this)
                        .load(R.drawable.snow)
                        .into(binding.imageMain)
                }
                if (i.icon == "50d" || i.icon == "50n") {
                    Glide.with(this)
                        .load(R.drawable.fog)
                        .into(binding.imageMain)
                }
            }
        })

        //prikaz prognoze za ZG kad se pokrene
        getZagreb()
        //viM.getWeather("Zagreb") - dohvaca "donji grad"

        //search
        val searchEditText = binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(Color.WHITE)

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                Preferences.getInstance(requireActivity())
                sharedPrefs.setValueOrNull("city", query!!)

                if (!query.isNullOrEmpty()) {
                    //dohvati info za trazeni grad
                    viM.getWeather(query)
                    //clear search
                    binding.searchView.setQuery("", false)
                    binding.searchView.clearFocus()
                    binding.searchView.isIconified = true
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        //5 dana botun, vodi na drugi fragment
        binding.next5Days.setOnClickListener {
            //debugging
            //Log.d("WeatherToday", "5 Days Button Clicked")
            val foreCastFragment = ForecastFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, foreCastFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }



    }

    private fun getZagreb() {
        // Latitude and longitude of Zagreb
        val latitude = 45.81444
        val longitude = 15.97798

        // Use requireContext() to get the context of the fragment
        val myprefs = Preferences(requireContext())
        myprefs.setValue("lon", longitude.toString())
        myprefs.setValue("lat", latitude.toString())

    }

}




