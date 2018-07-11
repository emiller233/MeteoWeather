/**
*  Copyright 2015 SmartThings
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*  SmartWeather Station
*
*  Author: SmartThings
*
*  Date: 2013-04-30
*
*	Updates by Barry A. Burke (storageanarchy@gmail.com)
*	Date: 2017 - 2018
*
*	1.0.00 - Initial Release
*	1.0.01 - Added PurpleAir Air Quality Index (AQI)
*	1.0.02 - Fixed New/Full moon dislays
*
*/
include 'asynchttp_v1'
import groovy.json.JsonSlurper

def getVersionNum() { return "1.0.02" }
private def getVersionLabel() { return "Meteobridge Weather Station, version ${getVersionNum()}" }

metadata {
    definition (name: "Meteobridge Weather Station", namespace: "sandood", author: "sandood") {
        capability "Illuminance Measurement"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Water Sensor"
        capability "Sensor"
        // capability "Polling"
        capability "Refresh"

        attribute "locationName", "string"
        attribute "dewpoint", "string"
        attribute "pressure", "string"
        //attribute "pressureDisplay", "string"
        attribute "pressureTrend", "string"
        attribute "highTemp", "string"
        attribute "lowTemp", "string"
        attribute "highTempYesterday", "string"
        attribute "lowTempYesterday", "string"
        attribute "highHumYesterday", "string"
        attribute "lowHumYesterday", "string"
        attribute "highHumidity", "string"
        attribute "lowHumidity", "string"
        attribute "weather", "string"
        attribute "weatherIcon", "string"
        attribute "forecast", "string"
        attribute "forecastCode", "string"
        attribute "airQualityIndex", "string"
        attribute "aqi", "string"
        attribute "wind", "string"
        attribute "winddirection", "string"
        attribute "wind_gust", "string"
        attribute "windChill", "string"
        attribute "winddirection_deg", "string"
        //attribute "windinfo", "string"
        attribute "heatIndex", "string"        
        attribute "uv_index", "string"
        attribute "forecastRule", "string"
        attribute "heatIndex", "string"
        attribute "precipYesterday", "string"
        attribute "precipToday", "string"
        attribute "precipLastHour", "string"
        attribute "precipRate", "string"
        //attribute "precipYesterdayDisplay", "string"
        //attribute "precipTodayDisplay", "string"
        //attribute "precipLastHourDisplay", "string"
        //attribute "precipRateDisplay", "string"
        attribute "water", "string"
        // attribute "alertKeys", "string"
        attribute "currentDate", "string"
        attribute "sunrise", "string"
        attribute "sunriseAPM", "string"
        attribute "sunriseEpoch", "string"
        attribute "sunset", "string"
        attribute "sunsetAPM", "string"
        attribute "sunsetEpoch", "string"
        attribute "dayHours", "string"
        attribute "dayMinutes", "string"
        attribute "isDay", "string"
        attribute "moonrise", "string"
        attribute "moonriseAPM", "string"
        attribute "moonriseEpoch", "string"
        attribute "moonset", "string"
        attribute "moonsetAPM", "string"
        attribute "moonsetEpoch", "string"
        attribute "solarRadiation", "string"
        attribute "evapotranspiration", "string"
        //attribute "etDisplay", "string"
        attribute "lunarSegment", "string"
        attribute "lunarAge", "string"
        attribute "lunarPercent", "string"
        attribute "moonPhase", "string"
        attribute "moonPercent", "string"
        attribute "moonDisplay", "string"
        attribute "moonInfo", "string"
  		attribute "lastSTupdate", "string"
        //attribute "meteoTemplate", "string"		// For debugging only
        //attribute "purpleAir", "string"			// For debugging only
        //attribute "meteoWeather", "string"		// For debugging only
        //attribute "iconErr", "string"				// For debugging only
        attribute "wundergroundObs", "string"		// For debugging only
        
        command "refresh"
		command "getWeatherReport"
    }

    preferences {
    	input (description: "Select the update frequency", 
        	title: "Update frequency", displayDuringSetup: true, type: 'paragraph', element: 'paragraph')
        input ("updateMins", "enum", title: "Update frequency", required: true, displayDuringSetup: true, defaultValue: '5', options: ['1','5','10','15','30'])
        
        input (description: "Setup Meteobridge access", 
        	title: "Meteobridge Setup", displayDuringSetup: true, type: 'paragraph', element: 'paragraph')
        input "zipCode", "text", title: "Zip Code or PWS (optional)", required: false, displayDuringSetup: true
        input "meteoIP", "string", title:"Meteobridge IP Address", description: "Eenter your Meteobridge's IP Address", required: true, displayDuringSetup: true
 		input "meteoPort", "string", title:"Meteobridge Port", description: "Enter your Meteobridge's Port", defaultValue: 80 , required: true, displayDuringSetup: true
    	input "meteoUser", "string", title:"Meteobridge User", description: "Enter your Meteobridge's username", required: true, defaultValue: 'meteobridge', displayDuringSetup: true
    	input "meteoPassword", "password", title:"Meteobridge Password", description: "Enter your Meteobridge's password", required: true, displayDuringSetup: true
        
        input (description: "Enter PurpleAir Sensor ID",
        	title: "PurpleAir Sensor", displayDuringSetup: true, type: 'paragraph', element: 'paragraph')
        input ("purpleID", "string", title: 'Purple Air Sensor ID (optional)', description: 'Enter your PurpleAir Sensor ID', required: false, displayDuringSetup: true)
        
        input (description: "Setting the Barometer Pressure units (optional)", 
            title: "Pressure units", displayDuringSetup: true, type: "paragraph", element: "paragraph")
        input ("pres_units", "enum", title: "Pressure units", required: false, displayDuringSetup: true, 
			options: [
		        "press_in":"Inches",
		        "press_mb":"milli bars"
            ])
        input (description: "Setting the distance units (optional)",
			title: "Distance Units", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        input ("dist_units", "enum", title: "Distance units", required: false, displayDuringSetup: true, 
			options: [
		        "dist_mi":"Miles",
		        "dist_km":"Kilometers"
            ])
        input (description: "Setting the Height units (optional)",
			title: "Height Units", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        input("height_units", "enum", title: "Height units", required: false, displayDuringSetup: true, 
			options: [
                "height_in":"Inches",
                "height_mm":"Millimiters"
            ])
        input (description: "Setting the Speed units (optional)",
			title: "Speed Units", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        input("speed_units", "enum", title: "Speed units", required: false, displayDuringSetup: true, 
			options: [
                "speed_mph":"Miles per Hour",
                "speed_kph":"Kilometers per Hour"
            ])
        input "weather", "device.smartweatherStationTile", title: "Weather...", multiple: true, required: false
    }
    
    tiles(scale: 2) {
        multiAttributeTile(name:"temperatureDisplay", type:"generic", width:6, height:4, canChangeIcon: false) {
        // multiAttributeTile(name:"temperatureDisplay", type:"thermostat", width:6, height:4, canChangeIcon: false) {
            tileAttribute("device.temperatureDisplay", key: "PRIMARY_CONTROL") {
                attributeState("default", label:'${currentValue}', defaultValue: true,
					backgroundColors:[
		                [value: 31, color: "#153591"],
		                [value: 44, color: "#1e9cbb"],
		                [value: 59, color: "#90d2a7"],
		                [value: 74, color: "#44b621"],
		                [value: 84, color: "#f1d801"],
		                [value: 95, color: "#d04e00"],
		                [value: 96, color: "#bc2323"]
                    ])
            }
            //tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
            //    attributeState("default", label:'${currentValue}%', unit:"%")
            //}
            //tileAttribute('device.weather', key: 'OPERATING_STATE') {
            //	attributeState('default', label: '${currentValue}', defaultValue: true, backgroundColors: null)
            //}
            tileAttribute("device.weatherIcon", key: "SECONDARY_CONTROL" /* decoration: "flat", inactiveLabel: false, width: 1, height: 1*/) {
                attributeState "chanceflurries", 	icon:"st.custom.wu1.chanceflurries", 	label: "Chance of Flurries"
                attributeState "chancerain", 		icon:"st.custom.wu1.chancerain", 		label: "Chance of Rain"
                attributeState "chancesleet", 		icon:"st.custom.wu1.chancesleet", 		label: "Chance of Sleet"
                attributeState "chancesnow", 		icon:"st.custom.wu1.chancesnow", 		label: "Chance of Snow"
                attributeState "chancetstorms", 	icon:"st.custom.wu1.chancetstorms", 	label: "Chance of Thunderstorms"
                attributeState "clear", 			icon:"st.custom.wu1.clear", 			label: "Clear"
                attributeState "cloudy", 			icon:"st.custom.wu1.cloudy", 			label: "Overcast"
                attributeState "flurries", 			icon:"st.custom.wu1.flurries", 			label: "Flurries"
                attributeState "fog", 				icon:"st.custom.wu1.fog", 				label: "Foggy"
                attributeState "hazy", 				icon:"st.custom.wu1.hazy", 				label: "Hazy"
                attributeState "mostlycloudy", 		icon:"st.custom.wu1.mostlycloudy", 		label: "Mostly Cloudy"
                attributeState "mostlysunny", 		icon:"st.custom.wu1.mostlysunny", 		label: "Mostly Sunny"
                attributeState "partlycloudy", 		icon:"st.custom.wu1.partlycloudy", 		label: "Partly Cloudy"
                attributeState "partlysunny", 		icon:"st.custom.wu1.partlysunny", 		label: "Partly Sunny"
                attributeState "rain", 				icon:"st.custom.wu1.rain", 				label: "Rain"
                attributeState "sleet",				icon:"st.custom.wu1.sleet", 			label: "Sleet"
                attributeState "snow", 				icon:"st.custom.wu1.snow", 				label: "Snow"
                attributeState "sunny", 			icon:"st.custom.wu1.sunny", 			label: "Sunny"
                attributeState "tstorms", 			icon:"st.custom.wu1.tstorms", 			label: "Thunderstorms"
                attributeState "cloudy", 			icon:"st.custom.wu1.cloudy", 			label: "Overcast"
                //attributeState "partlycloudy", 		icon:"st.custom.wu1.partlycloudy", 	label: ""
                attributeState "nt_chanceflurries", icon:"st.custom.wu1.nt_chanceflurries", label: "Chance of Flurries"
                attributeState "nt_chancerain", 	icon:"st.custom.wu1.nt_chancerain", 	label: "Chance of Rain"
                attributeState "nt_chancesleet", 	icon:"st.custom.wu1.nt_chancesleet", 	label: "Chance of Sleet"
                attributeState "nt_chancesnow", 	icon:"st.custom.wu1.nt_chancesnow", 	label: "Chance of Snow"
                attributeState "nt_chancetstorms", 	icon:"st.custom.wu1.nt_chancetstorms", 	label: "Chance of Thunderstorms"
                attributeState "nt_clear", 			icon:"st.custom.wu1.nt_clear", 			label: "Clear"
                attributeState "nt_cloudy", 		icon:"st.custom.wu1.nt_cloudy", 		label: "Cloudy"
                attributeState "nt_flurries", 		icon:"st.custom.wu1.nt_flurries", 		label: "Flurries"
                attributeState "nt_fog", 			icon:"st.custom.wu1.nt_fog", 			label: "Foggy"
                attributeState "nt_hazy", 			icon:"st.custom.wu1.nt_hazy", 			label: "Hazy"
                attributeState "nt_mostlycloudy", 	icon:"st.custom.wu1.nt_mostlycloudy", 	label: "Mostly Cloudy"
                attributeState "nt_mostlysunny", 	icon:"st.custom.wu1.nt_mostlysunny", 	label: "Mostly Clear Skies"
                attributeState "nt_partlycloudy", 	icon:"st.custom.wu1.nt_partlycloudy", 	label: "Partly Cloudy"
                attributeState "nt_partlysunny", 	icon:"st.custom.wu1.nt_partlysunny", 	label: "Partly Clear Skies"
                attributeState "nt_flurries", 		icon:"st.custom.wu1.nt_sleet", 			label: "Flurries"
                attributeState "nt_rain", 			icon:"st.custom.wu1.nt_rain", 			label: "Rain"
                attributeState "nt_sleet", 			icon:"st.custom.wu1.nt_sleet", 			label: "Sleet"
                attributeState "nt_snow", 			icon:"st.custom.wu1.nt_snow", 			label: "Snow"
                attributeState "nt_sunny", 			icon:"st.custom.wu1.nt_sunny", 			label: "Clear Skies"
                attributeState "nt_tstorms", 		icon:"st.custom.wu1.nt_tstorms", 		label: "Thunderstorms"
                attributeState "nt_cloudy", 		icon:"st.custom.wu1.nt_cloudy", 		label: "Overcast"
                // attributeState "nt_partlycloudy", 	icon:"st.custom.wu1.nt_partlycloudy", 	label: ""
            }
        }    
/*        standardTile("weatherIcon", "device.weatherIcon", decoration: "flat", inactiveLabel: false, width: 1, height: 1) {
            state "chanceflurries", icon:"st.custom.wu1.chanceflurries", label: ""
            state "chancerain", icon:"st.custom.wu1.chancerain", label: ""
            state "chancesleet", 		icon:"st.custom.wu1.chancesleet", label: ""
            state "chancesnow", 		icon:"st.custom.wu1.chancesnow", label: ""
            state "chancetstorms", 		icon:"st.custom.wu1.chancetstorms", label: ""
            state "clear", 				icon:"st.custom.wu1.clear", label: ""
            state "cloudy", 			icon:"st.custom.wu1.cloudy", label: ""
            state "flurries", 			icon:"st.custom.wu1.flurries", label: ""
            state "fog", 				icon:"st.custom.wu1.fog", label: ""
            state "hazy", 				icon:"st.custom.wu1.hazy", label: ""
            state "mostlycloudy", 		icon:"st.custom.wu1.mostlycloudy", label: ""
            state "mostlysunny", 		icon:"st.custom.wu1.mostlysunny", label: ""
            state "partlycloudy", 		icon:"st.custom.wu1.partlycloudy", label: ""
            state "partlysunny", 		icon:"st.custom.wu1.partlysunny", label: ""
            state "rain", 				icon:"st.custom.wu1.rain", label: ""
            state "sleet",				icon:"st.custom.wu1.sleet", label: ""
            state "snow", 				icon:"st.custom.wu1.snow", label: ""
            state "sunny", 				icon:"st.custom.wu1.sunny", label: ""
            state "tstorms", 			icon:"st.custom.wu1.tstorms", label: ""
            state "cloudy", 			icon:"st.custom.wu1.cloudy", label: ""
            state "partlycloudy", 		icon:"st.custom.wu1.partlycloudy", label: ""
            state "nt_chanceflurries", 	icon:"st.custom.wu1.nt_chanceflurries", label: ""
            state "nt_chancerain", 		icon:"st.custom.wu1.nt_chancerain", label: ""
            state "nt_chancesleet", 	icon:"st.custom.wu1.nt_chancesleet", label: ""
            state "nt_chancesnow", 		icon:"st.custom.wu1.nt_chancesnow", label: ""
            state "nt_chancetstorms", 	icon:"st.custom.wu1.nt_chancetstorms", label: ""
            state "nt_clear", 			icon:"st.custom.wu1.nt_clear", label: ""
            state "nt_cloudy", 			icon:"st.custom.wu1.nt_cloudy", label: ""
            state "nt_flurries", 		icon:"st.custom.wu1.nt_flurries", label: ""
            state "nt_fog", 			icon:"st.custom.wu1.nt_fog", label: ""
            state "nt_hazy", 			icon:"st.custom.wu1.nt_hazy", label: ""
            state "nt_mostlycloudy", 	icon:"st.custom.wu1.nt_mostlycloudy", label: ""
            state "nt_mostlysunny", 	icon:"st.custom.wu1.nt_mostlysunny", label: ""
            state "nt_partlycloudy", 	icon:"st.custom.wu1.nt_partlycloudy", label: ""
            state "nt_partlysunny", 	icon:"st.custom.wu1.nt_partlysunny", label: ""
            state "nt_sleet", 			icon:"st.custom.wu1.nt_sleet", label: ""
            state "nt_rain", 			icon:"st.custom.wu1.nt_rain", label: ""
            state "nt_sleet", 			icon:"st.custom.wu1.nt_sleet", label: ""
            state "nt_snow", 			icon:"st.custom.wu1.nt_snow", label: ""
            state "nt_sunny", 			icon:"st.custom.wu1.nt_sunny", label: ""
            state "nt_tstorms", 		icon:"st.custom.wu1.nt_tstorms", label: ""
            state "nt_cloudy", 			icon:"st.custom.wu1.nt_cloudy", label: ""
            state "nt_partlycloudy", 	icon:"st.custom.wu1.nt_partlycloudy", label: ""
        }
*/
        standardTile('moonPhase', 'device.moonPhase', decoration: 'flat', inactiveLabel: false, width: 1, height: 1) {
        	state "New", 			 label: '', icon: "https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Lunar0.png"
            state "Waxing Crescent", label: '', icon: "https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Lunar1.png"
            state "First Quarter", 	 label: '', icon: "https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Lunar2.png"
            state "Waxing Gibbous",  label: '', icon: "https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Lunar3.png"
            state "Full", 			 label: '', icon: "https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Lunar4.png"
            state "Waning Gibbous",  label: '', icon: "https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Lunar5.png"
            state "Third Quarter", 	 label: '', icon: "https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Lunar6.png"
            state "Waning Crescent", label: '', icon: "https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Lunar7.png"          
        }
        standardTile('moonDisplay', 'device.moonDisplay', decoration: 'flat', inactiveLabel: false, width: 1, height: 1) {
        	state 'Moon-waning-000', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-000.png'
        	state 'Moon-waning-005', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-005.png'
            state 'Moon-waning-010', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-010.png'
        	state 'Moon-waning-015', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-015.png'
            state 'Moon-waning-020', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-020.png'
        	state 'Moon-waning-025', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-025.png'
            state 'Moon-waning-030', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-030.png'
        	state 'Moon-waning-035', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-035.png'
            state 'Moon-waning-040', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-040.png'
        	state 'Moon-waning-045', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-045.png'
            state 'Moon-waning-050', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-050.png'
        	state 'Moon-waning-055', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-055.png'
            state 'Moon-waning-060', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-060.png'
        	state 'Moon-waning-065', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-065.png'
            state 'Moon-waning-070', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-070.png'
        	state 'Moon-waning-075', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-075.png'
        	state 'Moon-waning-080', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-080.png'
        	state 'Moon-waning-085', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-085.png'
            state 'Moon-waning-090', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-090.png'
        	state 'Moon-waning-095', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-095.png'
            state 'Moon-waning-100', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waning-100.png'
            state 'Moon-waxing-000', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-000.png'
        	state 'Moon-waxing-005', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-005.png'
            state 'Moon-waxing-010', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-010.png'
        	state 'Moon-waxing-015', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-015.png'
            state 'Moon-waxing-020', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-020.png'
        	state 'Moon-waxing-025', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-025.png'
            state 'Moon-waxing-030', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-030.png'
        	state 'Moon-waxing-035', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-035.png'
            state 'Moon-waxing-040', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-040.png'
        	state 'Moon-waxing-045', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-045.png'
            state 'Moon-waxing-050', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-050.png'
        	state 'Moon-waxing-055', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-055.png'
            state 'Moon-waxing-060', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-060.png'
        	state 'Moon-waxing-065', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-065.png'
            state 'Moon-waxing-070', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-070.png'
        	state 'Moon-waxing-075', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-075.png'
        	state 'Moon-waxing-080', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-080.png'
        	state 'Moon-waxing-085', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-085.png'
            state 'Moon-waxing-090', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-090.png'
        	state 'Moon-waxing-095', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-095.png'
            state 'Moon-waxing-100', label: '', icon: 'https://raw.githubusercontent.com/SANdood/MeteoWeather/master/images/Moon-waxing-100.png'
        }
        valueTile("mooninfo", "device.moonInfo", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "windinfo", label: '${currentValue}'
        }
        valueTile("lastSTupdate", "device.lastSTupdate", inactiveLabel: false, width: 1, height: 2, decoration: "flat", wordWrap: true) {
            state("default", label: 'Updated\n${currentValue}')
        }
        valueTile("heatIndex", "device.heatIndex", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'Heat Index\n${currentValue}°'
        }
        valueTile("windChill", "device.windChill", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'Wind\nChill\n${currentValue}°'
        }
        valueTile("weather", "device.weather", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'${currentValue}'
        }
        valueTile("locationTile", "device.locationName", inactiveLabel: false, width: 1, height: 2, decoration: "flat", wordWrap: true) {
            state "default", label:'\nT\'day ->\n\nLows & Highs\n\nY\'day ->\n'
        }
        valueTile("precipYesterday", "device.precipYesterdayDisplay", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'Precip\nY\'day\n${currentValue}'
        }
        valueTile("precipToday", "device.precipTodayDisplay", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'Precip\nT\'day\n${currentValue}'
        }
        valueTile("precipLastHour", "device.precipLastHourDisplay", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'Precip\nLast Hr\n${currentValue}'
        }
        valueTile("precipRate", "device.precipRateDisplay", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'Precip\n${currentValue}\nper hr'
        }
        standardTile("refresh", "device.weather", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: "", action: "refresh", icon:"st.secondary.refresh"
        }
        valueTile("forecast", "device.forecast", inactiveLabel: false, width: 5, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'${currentValue}'
        }
        valueTile("sunrise", "device.sunriseAPM", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'Sun\nRise\n${currentValue}'
        }
        valueTile("sunset", "device.sunsetAPM", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'Sun\nSet\n${currentValue}'
        }
        valueTile("moonrise", "device.moonriseAPM", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'Moon\nRise\n${currentValue}'
        }
        valueTile("moonset", "device.moonsetAPM", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'Moon\nSet\n${currentValue}'
        }
        valueTile("daylight", "device.dayHours", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'Daylight\n${currentValue}'
        }
        valueTile("light", "device.illuminance", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'Lux\n${currentValue}'
        }
        valueTile("evo", "device.etDisplay", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'ET\n${currentValue}\nper hr'
        }
        valueTile("uv_index", "device.uv_index", inactiveLabel: false, decoration: "flat") {
            state "uv_index", label: 'UV\nIndex\n${currentValue}', unit: "UV Index"
        }
        standardTile("water", "device.water", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: 'updating...', icon: "st.unknown.unknown.unknown"
            state "wet",        icon: "st.alarm.water.wet",        backgroundColor:"#ff9999"
            state "dry",        icon: "st.alarm.water.dry",        backgroundColor:"#99ff99"
        }
        valueTile("dewpoint", "device.dewpoint", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'Dew\nPoint\n${currentValue}°'
        }
        valueTile("pressure", "device.pressureDisplay", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: '${currentValue}'
        }
        valueTile("solarRadiation", "device.solarRadiation", inactiveLabel: false, width: 1, height: 1, decoration: "flat", wordWrap: true) {
            state "solarRadiation", label: 'Solar\nRadiation\n${currentValue} W/m²'
        }
        valueTile("windinfo", "device.windinfo", inactiveLabel: false, width: 2, height: 1, decoration: "flat", wordWrap: true) {
            state "windinfo", label: 'Wind ${currentValue}'
        }
        valueTile('aqi', 'device.airQualityIndex', inactiveLabel: false, width: 1, height: 1, decoration: 'flat', wordWrap: true) {
        	state 'default', label: 'AQI\n${currentValue}',
            	backgroundColors: [
                	[value:   0, color: '#44b621'],		// Green - Good
                    [value:  50, color: '#44b621'],
                    [value:  51, color: '#f1d801'],		// Yellow - Moderate
                    [value: 100, color: '#f1d801'],
                    [value: 101, color: '#d04e00'],		// Orange - Unhealthy for Sensitive groups
                    [value: 150, color: '#d04e00'],
                    [value: 151, color: '#bc2323'],		// Red - Unhealthy
                    [value: 200, color: '#bc2323'],
                    [value: 201, color: '#800080'],		// Purple - Very Unhealthy
                    [value: 300, color: '#800080'],
                    [value: 301, color: '#800000']		// Maroon - Hazardous
                    //[value: 301, color: '#ff2c14']
                ]
        }
        valueTile("temperature2", "device.temperature", width: 1, height: 1, canChangeIcon: true) {
            state "temperature", label: '${currentValue}°',
				backgroundColors:[
		            [value: 31, color: "#153591"],
		            [value: 44, color: "#1e9cbb"],
		            [value: 59, color: "#90d2a7"],
		            [value: 74, color: "#44b621"],
		            [value: 84, color: "#f1d801"],
		            [value: 95, color: "#d04e00"],
		            [value: 96, color: "#bc2323"]
            	]
        }
        valueTile("highTempYday", "device.highTempYesterday", width: 1, height: 1, canChangeIcon: true) {
            state "temperature", label: '${currentValue}',
				backgroundColors:[
		            [value: 31, color: "#153591"],
		            [value: 44, color: "#1e9cbb"],
		            [value: 59, color: "#90d2a7"],
		            [value: 74, color: "#44b621"],
		            [value: 84, color: "#f1d801"],
		            [value: 95, color: "#d04e00"],
		            [value: 96, color: "#bc2323"]
            ]
        }
        valueTile("lowTempYday", "device.lowTempYesterday", width: 1, height: 1, canChangeIcon: true) {
            state "temperature", label: '${currentValue}',
				backgroundColors:[
		            [value: 31, color: "#153591"],
		            [value: 44, color: "#1e9cbb"],
		            [value: 59, color: "#90d2a7"],
		            [value: 74, color: "#44b621"],
		            [value: 84, color: "#f1d801"],
		            [value: 95, color: "#d04e00"],
		            [value: 96, color: "#bc2323"]
            ]
        }
        valueTile("highTemp", "device.highTemp", width: 1, height: 1, canChangeIcon: true) {
            state "temperature", label: '${currentValue}',
				backgroundColors:[
		            [value: 31, color: "#153591"],
		            [value: 44, color: "#1e9cbb"],
		            [value: 59, color: "#90d2a7"],
		            [value: 74, color: "#44b621"],
		            [value: 84, color: "#f1d801"],
		            [value: 95, color: "#d04e00"],
		            [value: 96, color: "#bc2323"]
            ]
        }
        valueTile("lowTemp", "device.lowTemp", width: 1, height: 1, canChangeIcon: true) {
            state "temperature", label: '${currentValue}',
				backgroundColors:[
		            [value: 31, color: "#153591"],
		            [value: 44, color: "#1e9cbb"],
		            [value: 59, color: "#90d2a7"],
		            [value: 74, color: "#44b621"],
		            [value: 84, color: "#f1d801"],
		            [value: 95, color: "#d04e00"],
		            [value: 96, color: "#bc2323"]
            ]
        }
        valueTile("humidity", "device.humidity", decoration: "flat", width: 1, height: 1) {
			state("default", label: '${currentValue}%', unit: "%", defaultState: true, backgroundColors: [ //#d28de0")
          		[value: 10, color: "#00BFFF"],
                [value: 100, color: "#ff66ff"]
            ] )
		}
        valueTile("lowHumYday", "device.lowHumYesterday", decoration: "flat", width: 1, height: 1) {
			state("default", label: '${currentValue}%', unit: "%", defaultState: true, backgroundColors: [ //#d28de0")
          		[value: 10, color: "#00BFFF"],
                [value: 100, color: "#ff66ff"]
            ] )
		}
        valueTile("highHumYday", "device.highHumYesterday", decoration: "flat", width: 1, height: 1) {
			state("default", label: '${currentValue}%', unit: "%", defaultState: true, backgroundColors: [ //#d28de0")
          		[value: 10, color: "#00BFFF"],
                [value: 100, color: "#ff66ff"]
            ] )
		}
        valueTile("lowHumidity", "device.lowHumidity", decoration: "flat", width: 1, height: 1) {
			state("default", label: '${currentValue}%', unit: "%", defaultState: true, backgroundColors: [ //#d28de0")
          		[value: 10, color: "#00BFFF"],
                [value: 100, color: "#ff66ff"]
            ] )
		}
        valueTile("highHumidity", "device.highHumidity", decoration: "flat", width: 1, height: 1) {
			state("default", label: '${currentValue}%', unit: "%", defaultState: true, backgroundColors: [ //#d28de0")
          		[value: 10, color: "#00BFFF"],
                [value: 100, color: "#ff66ff"]
            ] )
		}
        standardTile('oneByTwo', 'device.logo', width: 1, height: 2, decoration: 'flat') {
        	state "default", defaultState: true
        }
        standardTile('oneByOne', 'device.logo', width: 1, height: 1, decoration: 'flat') {
        	state "default", defaultState: true
        }
        main(["temperature2"])
        details([	"temperatureDisplay", /* "humidity",*/ 
        			/*"weatherIcon", */ 'aqi', /* "weather" */ 'windChill', "heatIndex" , "dewpoint", "pressure", 'humidity', 
                    "moonDisplay", "mooninfo", "windinfo", "solarRadiation",  "water", 
                    "light", "uv_index", "evo", "precipYesterday", "precipToday",  "precipLastHour", 
                    "forecast", 'precipRate',  
                    "sunrise", "sunset", "daylight", "moonrise", "moonset",  'refresh', 
                     "locationTile", 'lowTemp', 'highTemp', 'lowHumidity', 'highHumidity', "lastSTupdate",
                    		'lowTempYday', 'highTempYday', 'lowHumYday', 'highHumYday', // "oneByOne",
                   /* "lastSTupdate", */])}
}

def noOp() {}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"
}

def installed() {
	state.meteoWeather = [:]
	initialize()
}

def uninstalled() {
	unschedule()
}

def updated() {
	log.info "Updated, settings: ${settings}"
    state.meteoWeatherVersion = getVersionLabel()
	unschedule()
    initialize()
}

def initialize() {
	log.info 'Initializing...'
    // Create the template using the latest preferences values (components defined at the bottom)
    state.meteoTemplate = forecastTemplate + yesterdayTemplate + currentTemplate
    send(name: 'meteoTemplate', value: state.meteoTemplate, displayed: false, isStateChange: true)
    
    def userpassascii = meteoUser + ':' + meteoPassword
	state.userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    
    // Schedule the updates
    def t = updateMins ?: '5'
    if (t == '1') {
    	runEvery1Minute(getMeteoWeather)
    } else {
    	"runEvery${t}Minutes"(getMeteoWeather)
    }
    runEvery10Minutes(updateWundergroundTiles)	// This doesn't change all that frequently
	runIn(2,getMeteoWeather,[overwrite: true])	// Have to wait to let the state change settle
    updateWundergroundTiles()
    
    if (purpleID) {
    	runEvery5Minutes(getPurpleAirAQI)			// Async Air Quality
    	getPurpleAirAQI()
    }
}

// handle commands
def poll() { refresh() }
def refresh() { getMeteoWeather(); updateWundergroundTiles(); getPurpleAirAQI() }
def getWeatherReport() { return state.meteoWeather }
def configure() { updated() }

// Execute the hubAction request
def getMeteoWeather() {
    log.info "getMeteoWeather()"
    if (!state.meteoWeatherVersion || (state.meteoWeatherVersion != getVersionLabel())) {
    	// if the version level of the code changes, silently run updated() and initialize()
        log.trace "Version changed, updating..."
    	updated()
        return
    }
    // Create the hubAction request based on updated preferences
    def hubAction = new physicalgraph.device.HubAction(
            method: "GET",
            path: "/cgi-bin/template.cgi",
            headers: [ HOST: "${meteoIP}:${meteoPort}", 'Authorization': state.userpass ],
            query: ['template': "{\"timestamp\":${now()}," + state.meteoTemplate, 'contenttype': 'application/json;charset=utf-8' ],
            null,
            [callback: meteoWeatherCallback]
        )
    try {
        sendHubCommand(hubAction)
    } catch (Exception e) {
    	log.error "sendHubCommand Exception $e on $hubAction"
    }
}

// Handle the hubAction response
void meteoWeatherCallback(physicalgraph.device.HubResponse hubResponse) {
	log.info "meteoWeatherCallback() status: " + hubResponse.status
    //log.debug "meteoWeatherCallback() headers: " + hubResponse.headers
    if ((hubResponse.status == 200) && hubResponse.json) {
		state.meteoWeather = hubResponse.json
        //log.debug "meteoWeatherCallback() json: " + hubResponse.json
        send(name: 'meteoWeather', value: "${hubResponse.json}", displayed: false, isStateChange: true)
        updateWeatherTiles()
    } else {
    	log.error "meteoWeatherCallback() - Missing hubResponse.json (${hubResponse.status})"
    }
}

// This updates the tiles with Weather Underground data
def updateWundergroundTiles() {
	log.info "updateWundergroundTiles()"
    def obs = get("conditions")?.current_observation
    if (obs) {
    	def weatherIcon 
    	if (state.meteoWeather?.current?.containsKey('isNight')) {
        	weatherIcon = (state.meteoWeather.current?.isNight == 1) ? 'nt_' + obs.icon : obs.icon
        } else {
			weatherIcon = obs.icon_url.split("/")[-1].split("\\.")[0]
        }
		send(name: "weather", value: obs.weather, descriptionText: 'Weather is currently '+obs.weather)
        send(name: 'wundergroundObs', value: obs, displayed: false)
        // state.weatherText = obs.weather
		send(name: "weatherIcon", value: weatherIcon, displayed: false)
	}
}

// This updates the tiles with Meteobridge data
def updateWeatherTiles() {
//    def obs = get("conditions")?.current_observation
//    if (obs) {
//		def weatherIcon = obs.icon_url.split("/")[-1].split("\\.")[0]
//		send(name: "weather", value: obs.weather)
//		send(name: "weatherIcon", value: weatherIcon, displayed: false)
//        send(name: "iconErr", value: null)
//	}
    if (state.meteoWeather != [:]) {
        // log.debug "meteoWeather: ${state.meteoWeather}"
        def now = new Date(state.meteoWeather.timestamp).format('HH:mm:ss MM/dd/yyyy',location.timeZone)
        sendEvent(name:"lastSTupdate", value: now, displayed: false)
		String unit = getTemperatureScale()
        String h = (height_units && (height_units == 'height_in')) ? '"' : 'mm'
        
        // Yesterday data
        if (state.meteoWeather.yesterday) {
        	String t = decString(state.meteoWeather.yesterday.highTemp, 1)
        	send(name: 'highTempYesterday', value: t, unit: unit, descriptionText: "High Temperature yesterday was ${t}°${unit}")
            t = decString(state.meteoWeather.yesterday.lowTemp, 1)
            send(name: 'lowTempYesterday', value: t, unit: unit, descriptionText: "Low Temperature yesterday was ${t}°${unit}")
            String hum = intString(state.meteoWeather.yesterday.highHum)
            send(name: 'highHumYesterday', value: hum, unit: "%", descriptionText: "High Humidity yesterday was ${hum}%")
            hum = intString(state.meteoWeather.yesterday.lowHum)
            send(name: 'lowHumYesterday', value: hum, unit: "%", descriptionText: "Low Humidity yesterday was ${hum}%")
            String ryd = decString(state.meteoWeather.yesterday.rainfall, 2)
            if (ryd != '--') {
				send(name: 'precipYesterday', value: ryd, unit: "${h}", descriptionText: "Precipitation yesterday was ${ryd}${h}")
                send(name: 'precipYesterdayDisplay', value: "${ryd}${h}", displayed: false)
            } else send(name: 'precipYesterdayDisplay', value: ryd, displayed: false)
        }

        // Today data
		if (state.meteoWeather.current != [:]) { 
        	if (state.meteoWeather.current.isDay?.isNumber() && (state.meteoWeather.current.isDay != device.currentValue('isDay'))) {
            	updateWundergroundTiles()
                send(name: 'isDay', value: state.meteoWeather.current.isDay.toString(), displayed: true, descriptionText: ((state.meteoWeather.current.isDay.toInteger() == 1) ? 'Daybreak' : 'Nightfall'))
            }
            String td = decString(state.meteoWeather.current.temperature, 1)
			send(name: "temperature", value: td, unit: unit, descriptionText: "Temperature is ${td}°${unit}")
            send(name: "temperatureDisplay", value: td + '°', unit: unit, displayed: false, descriptionText: "Temperature is ${td}°${unit}")
            String t = decString(state.meteoWeather.current.highTemp, 1)
            send(name: "highTemp", value: t, unit: unit, descriptionText: "High Temperature so far today is ${t}°${unit}")
            t = decString(state.meteoWeather.current.lowTemp, 1)
            send(name: "lowTemp", value: t , unit: unit, descriptionText: "Low Temperature so far today is ${t}°${unit}")
            t = decString(state.meteoWeather.current.heatIndex, 1)
			send(name: "heatIndex", value:t , unit: unit, descriptionText: "Heat Index is ${t}°${unit}")
			t = decString(state.meteoWeather.current.dewpoint, 1)
            send(name: "dewpoint", value: t , unit: unit, descriptionText: "Dewpoint is ${t}°${unit}")
			t = decString(state.meteoWeather.current.windChill, 1)
            send(name: "windChill", value: t, unit: unit, descriptionText: "Wind Chill is ${t}°${unit}")
            
            String hum = intString(state.meteoWeather.current.humidity)
			send(name: "humidity", value: hum, unit: "%", descriptionText: "Humidity is ${hum}%")
            hum = intString(state.meteoWeather.current.highHum)
            send(name: "highHumidity", value: hum, unit: "%", descriptionText: "High Humidity so far today is ${hum}%")
            hum = intString(state.meteoWeather.current.lowHum)
            send(name: "lowHumidity", value: hum, unit: "%", descriptionText: "Low Humidity so far today is ${hum}%")
            def uv = decString(state.meteoWeather.current.uvIndex,1)
            send(name: "uv_index", value: "${uv}", descriptionText: "UV Index is ${uv}" )
           
            if (state.meteoWeather.current.solarRadiation != null) {
            	def val = Math.round(state.meteoWeather.current.solarRadiation.toFloat())
				send(name: "solarRadiation", value: val, units: 'W/m²', descriptionText: "Solar radiation is ${val} W/m²")
            } else {
            	send(name: "solarradiation", value: '--', displayed: false)
            }
        
		// Barometric Pressure        
			def pressure_trend_text
			switch (state.meteoWeather.current.pressureTrend) {
				case "FF" :
					pressure_trend_text = "Falling Quickly"
					break;
				case "FS":
					pressure_trend_text = "Falling Slowly"
					break;
				case "ST":
                case "N/A":
					pressure_trend_text = "Steady"
					break;
				case "RS":
					pressure_trend_text = "Rising Slowly"
					break;
				case "RF":
					pressure_trend_text = "Rising Quickly"
					break;
				default:
					pressure_trend_text = " "
			}
			def pv = (pres_units && (pres_units == 'press_in')) ? 'inHg' : 'mmHg'
            def pr = decString(state.meteoWeather.current.pressure, 2)
            send(name: 'pressure', value: pr, unit: pv, displayed: false, descriptionText: "Barometric Pressure is ${pr} ${pv}")
			send(name: 'pressureDisplay', value: "${pr}\n${pv}\n${pressure_trend_text}", descriptionText: "Barometric Pressure is ${pr} ${pv} - ${pressure_trend_text}")
            send(name: 'pressureTrend', value: pressure_trend_text, displayed: false, descriptionText: "Barometric Pressure trend is ${pressure_trend_text}")
       
		// Rain Yesterday, Rain Today, Rain Last Hour
        	String rlh = decString(state.meteoWeather.current.rainLastHour, 2)
            if (rlh != '--') {
				send(name: 'precipLastHourDisplay', value: "${rlh}${h}", displayed: false)
            	send(name: 'precipLastHour', value: rlh, unit: "${h}", descriptionText: "Precipitation in the Last Hour was ${rlh}${h}")
            } else send(name: 'precipLastHourDisplay', value: rlh, displayed: false)
            
            String rtd = decString(state.meteoWeather.current.rainfall, 2)
			if (rtd != '--') {
            	send(name: 'precipTodayDisplay', value:  "${rtd}${h}", displayed: false)
            	send(name: 'precipToday', value: rtd, unit: "${h}", descriptionText: "Precipitation so far today is ${rtd}${h}")
            } else send(name: 'precipTodayDisplay', value:  rtd, displayed: false)
            
            String rrt = decString(state.meteoWeather.current.rainRate, 2)
            if (rrt != '--') {
            	send(name: 'precipRateDisplay', value:  "${rrt}${h}", displayed: false)
            	send(name: 'precipRate', value: rrt, unit: "${h}/hr", descriptionText: "Precipitation rate is ${rrt}${h}/hour")
            } else send(name: 'precipRateDisplay', value:  rrt, displayed: false)
            
			if (state.meteoWeather.current.rainLastHour?.isNumber() && (state.meteoWeather.current.rainLastHour.toFloat() > 0)) {
				sendEvent( name: 'water', value: "wet" )
			} else {
				sendEvent( name: 'water', value: "dry" )
			}
            
            String et = decString(state.meteoWeather.current.evapotranspiration, 3)
            // log.debug "ET: ${et}"
            if (et != '--') {
            	send(name: "evapotranspiration", value: et, unit: "${h}", descriptionText: "Evapotranspiration rate is ${et}${h}/hour")
                send(name: "etDisplay", value: "${et}${h}", displayed: false)
            } else send(name: "etDisplay", value: et, displayed: false)

		// Wind 		
			String s = (speed_units && (speed_units == 'speed_mph')) ? 'mph' : 'kph'
            def winfo = "${state.meteoWeather.current.windDirText} (${state.meteoWeather.current.windDegrees.toInteger()}°) @ ${state.meteoWeather.current.windSpeed} ${s}\n(Gust: ${state.meteoWeather.current.windGust} ${s})"
			send(name: "windinfo", value: winfo, displayed: false, descriptionText: 'Wind is ' + winfo)
			send(name: "wind_gust", value: "${state.meteoWeather.current.windGust}", descriptionText: "Winds gusting to ${state.meteoWeather.current.windGust} ${s}")
			send(name: "winddirection", value: "${state.meteoWeather.current.windDirText}", descriptionText: "Winds from the ${state.meteoWeather.current.windDirText}")
			send(name: "winddirection_deg", value: "${state.meteoWeather.current.windDegrees.toInteger()}", descriptionText: "Winds from ${state.meteoWeather.current.windDegrees.toInteger()}°" )
			send(name: "wind", value: "${state.meteoWeather.current.windSpeed}", descriptionText: "Wind speed is ${state.meteoWeather.current.windSpeed} ${s}")

			if (location.name != device.currentValue("locationName")) {
				send(name: "locationName", value: location.name, isStateChange: true, descriptionText: "Location is ${loc}")
			}

		// Date stuff
        	if ( ((state.meteoWeather.current.date != "") && (state.meteoWeather.current.date != device.currentValue('currentDate'))) ||
            		((state.meteoWeather.current.sunrise != "") && (state.meteoWeather.current.sunrise != device.currentValue('sunrise'))) ||
                    ((state.meteoWeather.current.sunset != "") && (state.meteoWeather.current.sunset != device.currentValue('sunset'))) ||
                    ((state.meteoWeather.current.dayHours != "") && (state.meteoWeather.current.dayHours != device.currentValue('dayHours'))) ||
                    ( /*(state.meteoWeather.current.moonrise != "") && */ (state.meteoWeather.current.moonrise != device.currentValue('moonrise'))) || // sometimes there is no moonrise/set
                    ( /*(state.meteoWeather.current.moonset != "") &&  */ (state.meteoWeather.current.moonset != device.currentValue('moonset'))) ) {
            	// If any Date/Time has changed, time to update them all
                
            	// Sunrise / sunset
                if (state.meteoWeather.current.sunrise != "") updateMeteoTime(state.meteoWeather.current.sunrise, 'sunrise') else clearMeteoTime('sunrise')
                if (state.meteoWeather.current.sunset != "")  updateMeteoTime(state.meteoWeather.current.sunset, 'sunset') else clearMeteoTime('sunset')
                if (state.meteoWeather.current.dayHours != "") {
                	send(name: "dayHours", value: state.meteoWeather.current.dayHours, descriptionText: state.meteoWeather.current.dayHours + ' of daylight today')
                } else {
                	send(name: 'dayHours', value: "", displayed: false)
                }
                if (state.meteoWeather.current.dayMinutes?.isNumber()) {
                	send(name: "dayMinutes", value: state.meteoWeather.current.dayMinutes, displayed: true, descriptionText: state.meteoWeather.current.dayMinutes +' minutes of daylight today')
                }

            	// Moonrise / moonset
                if (state.meteoWeather.current.moonrise != "") updateMeteoTime(state.meteoWeather.current.moonrise, 'moonrise') else clearMeteoTime('moonrise')
                if (state.meteoWeather.current.moonset != "")  updateMeteoTime(state.meteoWeather.current.moonset, 'moonset') else clearMeteoTime('moonset')
                
                // update the date
                if (state.meteoWeather.current.date != "") {
                	send(name: 'currentDate', value: state.meteoWeather.current.date, displayed: false)
                } else {
                	send(name: 'currentDate', value: "", displayed: false)
                }
			}
            
         // Lux estimator
            def lux = estimateLux()
			send(name: "illuminance", value: lux, descriptionText: "Lux is ${lux} (est)")
            
    	// Forecast
    		if (state.meteoWeather.forecast?.text != null) {
    			send(name: 'forecast', value: state.meteoWeather.forecast?.text, descriptionText: "Davis Forecast: " + state.meteoWeather.forecast?.text)
        		send(name: "forecastCode", value: state.meteoWeather.forecast?.code, descriptionText: "Davis Forecast Rule #${state.meteoWeather.forecast?.code}")
        	}
            
        // Lunar Phases
        	String xn = 'x'				// For waxing/waning below
            String phase = '--'
        	if (state.meteoWeather.current.lunarSegment?.isNumber()) {
            	switch (state.meteoWeather.current.lunarSegment.toInteger()) {
                	case 0: 
                    	phase = 'New'
                        break;
                    case 1:
                    	phase = 'Waxing Crescent'
                        break;
                    case 2:
                    	phase = 'First Quarter'
                        break;
                    case 3:
                    	phase = 'Waxing Gibbous'
                        break;
                    case 4:
                    	phase = 'Full'
                        xn = 'n'
                        break;
                    case 5:
                    	phase = 'Waning Gibbous'
                        xn = 'n'
                        break;
                    case 6:
                    	phase = 'Third Quarter'
                        xn = 'n'
                        break;
                    case 7:
                    	phase = 'Waning Crescent'
                        xn = 'n'
                        break;
                }
            	send(name: 'moonPhase', value: phase, descriptionText: 'The Moon\'s phase is ' + phase)
                send(name: 'lunarSegment', value: state.meteoWeather.current.lunarSegment.toString(), displayed: false)
                String l = state.meteoWeather.current.lunarAge.toString()
                send(name: 'lunarAge', value: l, units: 'days', displayed: false, descriptionText: "The Moon is ${l} days old" )          
            }
            if (state.meteoWeather.current.lunarPercent?.isNumber()) {
            	String lpct = state.meteoWeather.current.lunarPercent.toString()
            	send(name: 'lunarPercent', value: lpct, displayed: true, unit: '%', descriptionText: "The Moon is ${lpct} lit")
                String pcnt = (phase == 'New') ? '000' : ((phase == 'Full') ? '100' : sprintf('%03d', (Math.round(state.meteoWeather.current.lunarPercent.toFloat() / 5.0) * 5).toInteger()))
                // pcnt = (Math.round(pcnt / 5) * 5) as Integer
                String pname = 'Moon-wa' + xn + 'ing-' + pcnt
                // log.debug "Lunar Percent by 5s: ${pcnt} - ${pname}"
                send(name: 'moonPercent', value: pcnt, displayed: false, unit: '%')
                send(name: 'moonDisplay', value: pname, displayed: false)
                if (state.meteoWeather.current.lunarAge.isNumber()) {
                	String sign = (xn == 'x') ? '+' : '-'
                    if ((phase == 'New') || (phase == 'Full')) sign = ''
                    String dir = (sign != '') ? "Wa${xn}ing" : phase
                	String info = "${dir}\n${state.meteoWeather.current.lunarPercent.toString()}%\nDay ${state.meteoWeather.current.lunarAge.toString()}"
                    send(name: 'moonInfo', value: info, displayed: false)
                }
            }
        }
	}
}

private updateMeteoTime(timeStr, stateName) {
	def t = timeToday(timeStr, location.timeZone).getTime()
	def tAPM = new Date(t).format('h:mm a', location.timeZone).toLowerCase()
   	send(name: stateName, value: timeStr, displayed: false)
    send(name: stateName + 'APM', value: tAPM, descriptionText: stateName.capitalize() + ' at ' + tAPM)
    send(name: stateName + 'Epoch', value: t, displayed: false)
}

private clearMeteoTime(stateName) {
	send(name: stateName, value: "", displayed: false)
    send(name: stateName + 'APM', value: "", descriptionText: 'No ' + stateName + 'today')
    send(name: stateName + 'Epoch', value: null, displayed: false)
}

private get(feature) {
    getWeatherFeature(feature, zipCode)
}

private localDate(timeZone) {
    def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
    df.setTimeZone(TimeZone.getTimeZone(timeZone))
    df.format(new Date())
}

private send(map) {
    sendEvent(map)
}

String getWeatherText() {
	return device?.currentValue('weather')
}
private String decString( value, decimals ) {
	return (value == null) ? '--' : String.format("%.${decimals}f", value.toFloat().round(decimals))
}
private String intString( value ) {
	return (value == null) ? '--' : Math.round(value.toFloat())
}

private estimateLux() {
	// If we have it, use solarRadiation as a proxy for Lux 
	if (state.meteoWeather?.current?.solarRadiation?.isNumber()){
    	def lux = (state.meteoWeather.current.isNight > 0) ? 10 : Math.round(state.meteoWeather.current.solarRadiation / 0.225)	// Hack to approximate Aeon multi-sensor values
        return (lux < 10) ? 10 : ((lux > 1000) ? 1000 : lux)
    }
    // handle other approximations here
    def lux = 0
    def now = new Date().time
    if (state.meteoWeather.current.isDay > 0) {
        //day
        def weatherIcon = wundergroundObs?.icon
        switch(weatherIcon) {
        	case 'tstorms':
            	lux = 50
            	break
            case ['cloudy', 'fog', 'rain', 'sleet', 'snow', 'flurries',
                'chanceflurries', 'chancerain', 'chancesleet',
                'chancesnow', 'chancetstorms']:
                lux = 100
                break
            case 'mostlycloudy':
                lux = 250
                break
            case ['partlysunny', 'partlycloudy', 'hazy']:
                lux = 750
                break
            default:
                //sunny, clear
                lux = 1000
        }

        //adjust for dusk/dawn
        Float afterSunrise = now - device.currentValue('sunriseEpoch')
        Float beforeSunset = device.currentValue('sunsetEpoch') - now
        Float oneHour = 1000 * 60 * 60

        if(afterSunrise < oneHour) {
            //dawn
            lux = Math.round(lux * (afterSunrise/oneHour))
        } else if (beforeSunset < oneHour) {
            //dusk
            lux = Math.round(lux * (beforeSunset/oneHour))
        }
    } else {
        //night - always set to 10 for now
        //could do calculations for dusk/dawn too
        lux = 10
    }
    lux
}

void getPurpleAirAQI() {

    if (!settings.purpleID) {
    	send(name: 'airQualityIndex', value: null, displayed: false)
        send(name: 'aqi', value: null, displayed: false)
        return
    }
    log.info "getPurpleAirAQI()"
    def params = [
        uri: 'https://www.purpleair.com',
        path: '/json',
        query: [show: settings.purpleID]
        // body: ''
    ]
    asynchttp_v1.get(purpleAirResponse, params)
}

def purpleAirResponse(response, data) {
	try {
        // log.debug "Purple Air json response is: $response.json"
        send(name: 'purpleAir', value: response.json, displayed: false)
        def stats = [:]
        if (response.json.results[0]?.Stats) stats[0] = new JsonSlurper().parseText(response.json.results[0].Stats)
        if (stats[0]?.timeSinceModified?.isNumber() && (stats[0].timeSinceModified < 660000 )) {
        	if (response.json.results[1]?.Stats) stats[1] = new JsonSlurper().parseText(response.json.results[1].Stats) 
            if (stats[1]?.timeSinceModified?.isNumber() && (stats[1].timeSinceModified < 660000 )) {
            	// log.debug stats
                Float pm = (response.json.results[0]?.PM2_5Value?.toFloat() + response.json.results[1]?.PM2_5Value?.toFloat()) / 2.0
                String aqi = Math.round(pm_to_aqi(pm)).toString() // decString(pm_to_aqi(pm), 1)
                send(name: 'airQualityIndex', value: aqi, descriptionText: "Air Quality Index is ${aqi}")
                send(name: 'aqi', value: aqi, displayed: false)
                return
            } else {
               	log.warn "Purple Air sensor ${response.json.results[1].ID} at ${response.json.results[1].Label} returned stale data (${stats[1].timeSinceModified.toInteger() / 1000} seconds old)"
            }
        } else {
            log.warn "Purple Air sensor ${response.json.results[0].ID} at ${response.json.results[0].Label} returned stale data (${stats[0].timeSinceModified.toInteger() / 1000} seconds old)"
        }
        send(name: 'airQualityIndex', value: 'n/a', descriptionText: "Purple Air sensor ${settings.purpleID} appears to be offline")
        send(name: 'aqi', value: 'n/a', displayed: false)
    } catch (e) {
        log.error("Exception during Purple Air response processing", e)
    }
}

private Float pm_to_aqi(pm) {
	def aqi
	if (pm > 500) {
	  aqi = 500;
	} else if (pm > 350.5 && pm <= 500 ) {
	  aqi = remap(pm, 350.5, 500.5, 400, 500);
	} else if (pm > 250.5 && pm <= 350.5 ) {
	  aqi = remap(pm, 250.5, 350.5, 300, 400);
	} else if (pm > 150.5 && pm <= 250.5 ) {
	  aqi = remap(pm, 150.5, 250.5, 200, 300);
	} else if (pm > 55.5 && pm <= 150.5 ) {
	  aqi = remap(pm, 55.5, 150.5, 150, 200);
	} else if (pm > 35.5 && pm <= 55.5 ) {
	  aqi = remap(pm, 35.5, 55.5, 100, 150);
	} else if (pm > 12 && pm <= 35.5 ) {
	  aqi = remap(pm, 12, 35.5, 50, 100);
	} else if (pm > 0 && pm <= 12 ) {
	  aqi = remap(pm, 0, 12, 0, 50);
	}
	return aqi;
}

private def remap(value, fromLow, fromHigh, toLow, toHigh) {
    def fromRange = fromHigh - fromLow;
    def toRange = toHigh - toLow;
    def scaleFactor = toRange / fromRange;

    // Re-zero the value within the from range
    def tmpValue = value - fromLow;
    // Rescale the value to the to range
    tmpValue *= scaleFactor;
    // Re-zero back to the to range
    return tmpValue + toLow;
}

String getMeteoSensorID() {
    def version = state.meteoWeather?.version?.isNumber() ? state.meteoWeather.version : 1.0
    return ( version > 3.6 ) ? '*' : '0'
}

def getForecastTemplate() {
	return '"forecast":{"text":"[forecast-text:]","code":[forecast-rule]},"version":[mbsystem-swversion:1.0],'
}
def getYesterdayTemplate() {
	String s = getTemperatureScale() 
	String d = getMeteoSensorID() 
    return "\"yesterday\":{\"highTemp\":[th${d}temp-ydmax=${s}.2:null],\"lowTemp\":[th${d}temp-ydmin=${s}.2:null],\"highHum\":[th${d}hum-ydmax=.2:null],\"lowHum\":[th${d}hum-ydmin=.2:null]," + yesterdayRainfall + '},'
	//if (getTemperatureScale() != "C") {
	//	return '"yesterday":{"highTemp":[th0temp-ydmax=F.2:null],"lowTemp":[th0temp-ydmin=F.2:null],"highHum":[th0hum-ydmax=.2:null],"lowHum":[th0hum-ydmin=.2:null],' + yesterdayRainfall + '},'
	//} else {
	//	return '"yesterday":{"highTemp":[th0temp-ydmax=C.2:null],"lowTemp":[th0temp-ydmin=C.2:null],"highHum":[th0hum-ydmax=.2:null],"lowHum":[th0hum-ydmin=.2:null],' + yesterdayRainfall + '},'
	//}
}
def getCurrentTemplate() {
	String d = getMeteoSensorID()
	return "\"current\":{\"date\":\"[MM]/[DD]/[YYYY]\",\"humidity\":[th${d}hum-act=.2:null],\"indoorHum\":[thb${d}hum-act=.2:null]," + temperatureTemplate + currentRainfall + pressureTemplate + windTemplate +
			"\"pressureTrend\":\"[thb${d}seapress-delta1=enbarotrend:N/A]\",\"dayHours\":\"[mbsystem-daylength:]\",\"highHum\":[th${d}hum-dmax=.2:null],\"lowHum\":[th${d}hum-dmin=.2:null]," +
			"\"sunrise\":\"[mbsystem-sunrise:]\",\"sunset\":\"[mbsystem-sunset:]\",\"dayMinutes\":[mbsystem-daylength=mins.0:null],\"uvIndex\":[uv${d}index-act:null]," +
            "\"solarRadiation\":[sol${d}rad-act:null],\"lunarAge\":[mbsystem-lunarage:],\"lunarPercent\":[mbsystem-lunarpercent:],\"lunarSegment\":[mbsystem-lunarsegment:null]," +
            '"moonrise":"[mbsystem-moonrise:]","moonset":"[mbsystem-moonset:]","isDay":[mbsystem-isday=.0],"isNight":[mbsystem-isnight=.0]}}'
}

// #if{*[mbsystem-swversion:1.0]>=3.6*}#then#outdoor temp: [th*temp-act:--]°C#else#outdoor temp: [th0temp-act:--]°C#fi#
// #if{*[mbsystem-swversion:1.0]>=3.6*}#then#[th*temp-act=${s}.2:null]#else#[th0temp-act

def getTemperatureTemplate() { 
	String s = getTemperatureScale() 
    String d = getMeteoSensorID() 
	return "\"temperature\":[th${d}temp-act=${s}.2:null],\"dewpoint\":[th${d}dew-act=${s}.2:null],\"heatIndex\":[th${d}heatindex-act=${s}.2:null],\"windChill\":[wind${d}chill-act=${s}.2:null]," +
    		"\"indoorTemp\":[thb${d}temp-act=${s}.2:null],\"indoorDew\":[thb${d}dew-act=${s}.2:null],\"highTemp\":[th${d}temp-dmax=${s}.2:null],\"lowTemp\":[th${d}temp-dmin=${s}.2:null],"
}
def getPressureTemplate() {
	String p = (pres_units && (pres_units == 'press_in')) ? 'inHg' : 'mmHg'
    String d = getMeteoSensorID() 
	return "\"pressure\":[thb${d}seapress-act=${p}.2:null],"
}
def getYesterdayRainfall() {
	String r = (height_units && (height_units == 'height_in')) ? 'in' : ''
    String d = getMeteoSensorID()
	return "\"rainfall\":[rain${d}total-ydaysum=${r}.3:null]," 
}
def getCurrentRainfall() {
	String r = (height_units && (height_units == 'height_in')) ? 'in' : ''
    String d = getMeteoSensorID()
	return "\"rainfall\":[rain${d}total-daysum=${r}.3:null],\"rainLastHour\":[rain${d}total-sum1h=${r}.3:null],\"evapotranspiration\":[sol${d}evo-act=${r}.3:null],\"rainRate\":[rain${d}rate-act=${r}.3:null],"
}
def getWindTemplate() {
    String s = (speed_units && (speed_units == 'speed_mph')) ? 'mph' : 'kmh'
    String d = getMeteoSensorID()
	return "\"windGust\":[wind${d}wind-max10=${s}.2:null],\"windAvg\":[wind${d}wind-act=${s}.2:null],\"windDegrees\":[wind${d}dir-act:null],\"windSpeed\":[wind${d}wind-act=${s}.2:null],\"windDirText\":\"[wind${d}dir-act=endir:null]\","
}