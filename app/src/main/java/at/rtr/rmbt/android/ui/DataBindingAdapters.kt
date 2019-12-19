package at.rtr.rmbt.android.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import at.rmbt.client.control.IpProtocol
import at.rtr.rmbt.android.R
import at.rtr.rmbt.android.ui.view.SpeedLineChart
import at.rtr.rmbt.android.ui.view.WaveView
import at.rtr.rmbt.android.ui.view.curve.MeasurementCurveLayout
import at.rtr.rmbt.android.util.InfoWindowStatus
import at.rtr.rmbt.android.util.format
import at.specure.data.Classification
import at.specure.data.NetworkTypeCompat
import at.specure.data.entity.GraphItemRecord
import at.specure.info.TransportType
import at.specure.info.cell.CellNetworkInfo
import at.specure.info.cell.CellTechnology
import at.specure.info.ip.IpInfo
import at.specure.info.ip.IpStatus
import at.specure.info.network.NetworkInfo
import at.specure.info.network.WifiNetworkInfo
import at.specure.info.strength.SignalStrengthInfo
import at.specure.measurement.MeasurementState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.Calendar

@BindingAdapter("intText")
fun intText(textView: TextView, value: Int) {
    textView.text = value.toString()
}

/**
 * A Binding adapter that is used for change visibility of view
 */
@BindingAdapter("visibleOrGone")
fun View.setVisibleOrGone(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}

/**
 * A binding adapter that is used for show signal
 */
@BindingAdapter("signal")
fun AppCompatTextView.setSignal(signal: Int?) {

    text = if (signal != null) {
        String.format(context.getString(R.string.home_signal_value), signal)
    } else {
        "-"
    }
}

/**
 * A binding adapter that is used for show frequency
 */
@BindingAdapter("frequency")
fun AppCompatTextView.setFrequency(networkInfo: NetworkInfo?) {

    text = when (networkInfo) {
        is WifiNetworkInfo -> networkInfo.band.name
        is CellNetworkInfo -> networkInfo.band?.name
        else -> "-"
    }
}

/**
 * A binding adapter that is used for show frequency
 */
@BindingAdapter("frequencyVisibility")
fun View.frequencyVisibility(networkInfo: NetworkInfo?) {
    visibility =
        if (networkInfo != null && networkInfo is CellNetworkInfo && networkInfo.band == null) {
            View.GONE
        } else {
            View.VISIBLE
        }
}

/**
 * A binding adapter that is used for show information window
 * Information window display when user not doing action for 2 second
 */
@BindingAdapter(value = ["isConnected", "infoWindowStatus"], requireAll = true)
fun AppCompatTextView.showPopup(isConnected: Boolean, infoWindowStatus: InfoWindowStatus) {
    visibility = if (isConnected) {
        when (infoWindowStatus) {
            InfoWindowStatus.NONE -> {
                View.GONE
            }
            InfoWindowStatus.VISIBLE -> {
                View.VISIBLE
            }
            InfoWindowStatus.GONE -> {
                View.GONE
            }
        }
    } else {
        View.GONE
    }
}

/**
 * A binding adapter that is used for show network icon based on network type (WIFI/MOBILE),
 * and signalLevel(0..4)
 */
@BindingAdapter("signalLevel")
fun AppCompatImageView.setIcon(signalStrengthInfo: SignalStrengthInfo?) {

    if (signalStrengthInfo != null) {

        val transportType: TransportType? = signalStrengthInfo.transport

        when (signalStrengthInfo.signalLevel) {

            2 -> {
                if (transportType == TransportType.WIFI)
                    setImageResource(R.drawable.ic_wifi_2)
                else if (transportType == TransportType.CELLULAR)
                    setImageResource(R.drawable.ic_mobile_2)
            }
            3 -> {
                if (transportType == TransportType.WIFI)
                    setImageResource(R.drawable.ic_wifi_3)
                else if (transportType == TransportType.CELLULAR)
                    setImageResource(R.drawable.ic_mobile_3)
            }
            4 -> {
                if (transportType == TransportType.WIFI)
                    setImageResource(R.drawable.ic_wifi_4)
                else if (transportType == TransportType.CELLULAR)
                    setImageResource(R.drawable.ic_mobile_4)
            }
            else -> {
                if (transportType == TransportType.WIFI)
                    setImageResource(R.drawable.ic_wifi_1)
                else if (transportType == TransportType.CELLULAR)
                    setImageResource(R.drawable.ic_mobile_1)
            }
        }
    } else {
        setImageResource(R.drawable.ic_no_internet)
    }
}

/**
 * A binding adapter that is used for show network type
 */
@BindingAdapter("networkType")
fun AppCompatTextView.setNetworkType(networkInfo: NetworkInfo?) {

    text = when (networkInfo) {
        is WifiNetworkInfo -> context.getString(R.string.home_wifi)
        is CellNetworkInfo -> {
            val technology =
                CellTechnology.fromMobileNetworkType(networkInfo.networkType)?.displayName
            if (technology == null) {
                networkInfo.networkType.displayName
            } else {
                "$technology/${networkInfo.networkType.displayName}"
            }
        }
        else -> context.getString(R.string.home_attention)
    }
}

/**
 * A binding adapter that is used for show technology icon for mobile network
 */
@BindingAdapter("technology")
fun AppCompatImageView.setTechnologyIcon(networkInfo: NetworkInfo?) {

    if (networkInfo is CellNetworkInfo) {
        visibility = View.VISIBLE
        when (CellTechnology.fromMobileNetworkType(networkInfo.networkType)) {
            null -> {
                setImageDrawable(null)
            }
            CellTechnology.CONNECTION_2G -> {
                setImageResource(R.drawable.ic_2g)
            }
            CellTechnology.CONNECTION_3G -> {
                setImageResource(R.drawable.ic_3g)
            }
            CellTechnology.CONNECTION_4G -> {
                setImageResource(R.drawable.ic_4g)
            }
            CellTechnology.CONNECTION_5G -> {
                setImageResource(R.drawable.ic_5g)
            }
        }
    } else {
        visibility = View.GONE
    }
}

/**
 * A binding adapter that is used for show ip address icon
 */
@BindingAdapter(value = ["IpIcon"], requireAll = true)
fun ImageView.setIPAddressIcon(ipInfo: IpInfo?) {
    ipInfo?.let {
        val isIPV4 = it.protocol == IpProtocol.V4

        val res = when (it.ipStatus) {
            IpStatus.NO_INFO -> {
                isClickable = false
                if (isIPV4) R.drawable.ic_ipv4_gray else R.drawable.ic_ipv6_gray
            }
            IpStatus.NO_ADDRESS -> {
                isClickable = true
                if (isIPV4) R.drawable.ic_ipv4_red else R.drawable.ic_ipv6_red
            }
            IpStatus.NO_NAT -> {
                isClickable = true
                if (isIPV4) R.drawable.ic_ipv4_green else R.drawable.ic_ipv6_green
            }
            else -> {
                isClickable = true
                if (isIPV4) R.drawable.ic_ipv4_yellow else R.drawable.ic_ipv6_yellow
            }
        }
        setImageResource(res)
    }
}

@BindingAdapter("waveEnabled")
fun waveEnabled(view: WaveView, enabled: Boolean) {
    view.waveEnabled = enabled
}

val THRESHOLD_PING = listOf(0, 10, 25, 75) // 0ms, 10ms, 25ms, 75ms
/**
 * A binding adapter that is used for show ping value
 */
@BindingAdapter("pingMs")
fun AppCompatTextView.setPing(pingMs: Long) {

    if (pingMs > 0) {

        setCompoundDrawablesWithIntrinsicBounds(

            when (pingMs) {
                in THRESHOLD_PING[0]..THRESHOLD_PING[1] -> {
                    R.drawable.ic_small_ping_dark_green
                }
                in THRESHOLD_PING[1]..THRESHOLD_PING[2] -> {
                    R.drawable.ic_small_ping_light_green
                }
                in THRESHOLD_PING[2]..THRESHOLD_PING[3] -> {
                    R.drawable.ic_small_ping_yellow
                }
                else -> {
                    R.drawable.ic_small_ping_red
                }
            }, 0, 0, 0
        )
        text = context.getString(R.string.measurement_ping_value, pingMs)
        setTextColor(context.getColor(android.R.color.white))
    } else {
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_small_ping_gray, 0, 0, 0)
        setTextColor(context.getColor(R.color.text_white_transparency_40))
        text = context.getString(R.string.measurement_dash)
    }
}

val THRESHOLD_DOWNLOAD = listOf(0L, 1000000L, 2000000L, 30000000L) // 0mb, 1mb, 2mb, 30mb
/**
 * A binding adapter that is used for show download speed
 */
@BindingAdapter("downloadSpeedBps")
fun AppCompatTextView.setDownload(downloadSpeedBps: Long) {

    if (downloadSpeedBps > 0) {
        val downloadSpeedInMbps: Float = downloadSpeedBps / 1000000.0f

        setCompoundDrawablesWithIntrinsicBounds(

            when (downloadSpeedBps) {
                in THRESHOLD_DOWNLOAD[0] until THRESHOLD_DOWNLOAD[1] -> {
                    R.drawable.ic_small_download_red
                }
                in THRESHOLD_DOWNLOAD[1] until THRESHOLD_DOWNLOAD[2] -> {
                    R.drawable.ic_small_download_yellow
                }
                in THRESHOLD_DOWNLOAD[2] until THRESHOLD_DOWNLOAD[3] -> {
                    R.drawable.ic_small_download_light_green
                }
                else -> {
                    R.drawable.ic_small_download_dark_green
                }
            }, 0, 0, 0
        )
        text = context.getString(
            R.string.measurement_download_upload_speed,
            downloadSpeedInMbps.format()
        )
        setTextColor(context.getColor(android.R.color.white))
    } else {
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_small_download_gray, 0, 0, 0)
        setTextColor(context.getColor(R.color.text_white_transparency_40))
        text = context.getString(R.string.measurement_dash)
    }
}

val THRESHOLD_UPLOAD = listOf(0L, 500000L, 1000000L, 10000000L) // 0mb, 0.5mb, 1mb, 10mb
/**
 * A binding adapter that is used for show upload speed
 */
@BindingAdapter("uploadSpeedBps")
fun AppCompatTextView.setUpload(uploadSpeedBps: Long) {

    if (uploadSpeedBps > 0) {
        val uploadSpeedInMbps: Float = uploadSpeedBps / 1000000.0f

        setCompoundDrawablesWithIntrinsicBounds(

            when (uploadSpeedBps) {
                in THRESHOLD_UPLOAD[0] until THRESHOLD_UPLOAD[1] -> {
                    R.drawable.ic_small_upload_red
                }
                in THRESHOLD_UPLOAD[1] until THRESHOLD_UPLOAD[2] -> {
                    R.drawable.ic_small_upload_yellow
                }
                in THRESHOLD_UPLOAD[2] until THRESHOLD_UPLOAD[3] -> {
                    R.drawable.ic_small_upload_light_green
                }
                else -> {
                    R.drawable.ic_small_upload_dark_green
                }
            }, 0, 0, 0
        )
        text = context.getString(
            R.string.measurement_download_upload_speed,
            uploadSpeedInMbps.format()
        )
        setTextColor(context.getColor(android.R.color.white))
    } else {
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_small_upload_gray, 0, 0, 0)
        setTextColor(context.getColor(R.color.text_white_transparency_40))
        text = context.getString(R.string.measurement_dash)
    }
}

/**
 * A binding adapter that is used for show network icon based on network type (WIFI/MOBILE),
 * and signalLevel(0..4)
 */
@BindingAdapter("measurementSignalLevel")
fun AppCompatImageView.setSmallIcon(signalStrengthInfo: SignalStrengthInfo?) {

    when (signalStrengthInfo?.transport) {
        TransportType.WIFI -> {
            when (signalStrengthInfo.signalLevel) {
                2 -> {
                    setImageResource(R.drawable.ic_small_wifi_2)
                }
                3 -> {
                    setImageResource(R.drawable.ic_small_wifi_3)
                }
                4 -> {
                    setImageResource(R.drawable.ic_small_wifi_4)
                }
                else -> {
                    setImageResource(R.drawable.ic_small_wifi_1)
                }
            }
        }
        TransportType.CELLULAR -> {
            when (signalStrengthInfo.signalLevel) {
                2 -> {
                    setImageResource(R.drawable.ic_small_mobile_2)
                }
                3 -> {
                    setImageResource(R.drawable.ic_small_mobile_3)
                }
                4 -> {
                    setImageResource(R.drawable.ic_small_mobile_4)
                }
                else -> {
                    setImageResource(R.drawable.ic_small_mobile_1)
                }
            }
        }
        else -> {
            setImageResource(R.drawable.ic_small_no_internet)
        }
    }
}

/**
 * A binding adapter that is used for show download and upload data on graph
 */
@BindingAdapter("graphItems")
fun SpeedLineChart.setGraphItems(graphItems: List<GraphItemRecord>?) {
    addGraphItems(graphItems)
}

/**
 * A binding adapter that is used for clear download and upload data
 */
@BindingAdapter("reset")
fun SpeedLineChart.reset(measurementState: MeasurementState) {

    when (measurementState) {
        MeasurementState.IDLE, MeasurementState.INIT,
        MeasurementState.DOWNLOAD, MeasurementState.UPLOAD -> {
            reset()
        }
        else -> {
        }
    }
}

@BindingAdapter("speed")
fun MeasurementCurveLayout.setSpeed(speed: Long) {
    setBottomProgress(speed)
}

@BindingAdapter("percentage")
fun MeasurementCurveLayout.setPercents(percents: Int) {
    setTopProgress(percents)
}

@BindingAdapter("strength", "strengthMin", "strengthMax", requireAll = true)
fun MeasurementCurveLayout.setSignal(signalLevel: Int, strengthMin: Int, strengthMax: Int) {
    setSignalStrength(signalLevel, strengthMin, strengthMax)
}

@BindingAdapter("measurementPhase")
fun MeasurementCurveLayout.setMeasurementPhase(state: MeasurementState) {
    setMeasurementState(state)
}

@BindingAdapter("qosEnabled")
fun MeasurementCurveLayout.setQosEnabled(enabled: Boolean) {
    setQoSEnabled(enabled)
}

/**
 * A binding adapter that is used for show label of measurement state
 */
@BindingAdapter("labelMeasurementState")
fun AppCompatTextView.setLabelOfMeasurementState(measurementState: MeasurementState) {

    when (measurementState) {
        MeasurementState.IDLE, MeasurementState.INIT, MeasurementState.PING, MeasurementState.DOWNLOAD -> {
            text = context.getString(R.string.measurement_download)
        }
        MeasurementState.UPLOAD -> {
            text = context.getString(R.string.measurement_upload)
        }
        MeasurementState.QOS -> {
            text = context.getString(R.string.measurement_qos)
        }
        else -> {
        }
    }
}

@BindingAdapter("bottomSheetState")
fun ConstraintLayout.setBottomSheetState(state: Int) {
    val behavior = BottomSheetBehavior.from(this)
    behavior.state = state
}

/**
 * A binding adapter that is used for show date and time in history list
 */
@BindingAdapter("networkType", "time", requireAll = true)
fun AppCompatTextView.setTime(networkType: NetworkTypeCompat, time: Long) {

    val calendar: Calendar = Calendar.getInstance()
    calendar.timeInMillis = time
    text = calendar.format("dd.MM.yy, hh:mm:ss")

    setCompoundDrawablesWithIntrinsicBounds(

        when (networkType) {
            NetworkTypeCompat.TYPE_2G -> {
                R.drawable.ic_history_2g
            }
            NetworkTypeCompat.TYPE_3G -> {
                R.drawable.ic_history_3g
            }
            NetworkTypeCompat.TYPE_4G -> {
                R.drawable.ic_history_4g
            }
            NetworkTypeCompat.TYPE_WLAN -> {
                R.drawable.ic_history_wifi
            }
        }, 0, 0, 0
    )
}

/**
 * A binding adapter that is used for show download speed with classification icon in history list
 */
@BindingAdapter("speedDownload", "speedDownloadClassification", requireAll = true)
fun AppCompatTextView.setDownload(speedDownload: Double, speedDownloadClassification: Classification) {

    text = if (speedDownload > 0) {
        speedDownload.toFloat().format()
    } else {
        context.getString(R.string.measurement_dash)
    }
    setCompoundDrawablesWithIntrinsicBounds(getSpeedDownloadClassification(speedDownloadClassification), 0, 0, 0)
}

/**
 * A binding adapter that is used for show download speed with classification icon in results
 */
@BindingAdapter("speedDownloadLong", "speedDownloadClassification", requireAll = true)
fun AppCompatTextView.setDownload(speedDownload: Long, speedDownloadClassification: Classification) {

    text = if (speedDownload > 0) {
        (speedDownload / 1000).toFloat().format()
    } else {
        context.getString(R.string.measurement_dash)
    }
    setCompoundDrawablesWithIntrinsicBounds(getSpeedDownloadClassification(speedDownloadClassification), 0, 0, 0)
}

fun getSpeedDownloadClassification(speedDownloadClassification: Classification): Int {
    return when (speedDownloadClassification) {
        Classification.NONE -> {
            R.drawable.ic_small_download_gray
        }
        Classification.BAD -> {
            R.drawable.ic_small_download_red
        }
        Classification.NORMAL -> {
            R.drawable.ic_small_download_yellow
        }
        Classification.GOOD -> {
            R.drawable.ic_small_download_light_green
        }
        Classification.EXCELLENT -> {
            R.drawable.ic_small_download_dark_green
        }
    }
}

/**
 * A binding adapter that is used for show upload speed with classification icon in results
 */
@BindingAdapter("speedUploadLong", "speedUploadClassification", requireAll = true)
fun AppCompatTextView.setUpload(speedUpload: Long, speedUploadClassification: Classification) {
    text = if (speedUpload > 0) {
        (speedUpload.toFloat() / 1000).format()
    } else {
        context.getString(R.string.measurement_dash)
    }
    setCompoundDrawablesWithIntrinsicBounds(getSpeedUploadClassificationIcon(speedUploadClassification), 0, 0, 0)
}

/**
 * A binding adapter that is used for show upload speed with classification icon in history list
 */
@BindingAdapter("speedUpload", "speedUploadClassification", requireAll = true)
fun AppCompatTextView.setUpload(speedUpload: Double, speedUploadClassification: Classification) {

    text = if (speedUpload > 0) {
        speedUpload.toFloat().format()
    } else {
        context.getString(R.string.measurement_dash)
    }
    setCompoundDrawablesWithIntrinsicBounds(getSpeedUploadClassificationIcon(speedUploadClassification), 0, 0, 0)
}

fun getSpeedUploadClassificationIcon(speedUploadClassification: Classification): Int {
    return when (speedUploadClassification) {
        Classification.NONE -> {
            R.drawable.ic_small_upload_gray
        }
        Classification.BAD -> {
            R.drawable.ic_small_upload_red
        }
        Classification.NORMAL -> {
            R.drawable.ic_small_upload_yellow
        }
        Classification.GOOD -> {
            R.drawable.ic_small_upload_light_green
        }
        Classification.EXCELLENT -> {
            R.drawable.ic_small_upload_dark_green
        }
    }
}

/**
 * A binding adapter that is used for show download ping with classification icon in history list
 */
@BindingAdapter("ping", "pingClassification", requireAll = true)
fun AppCompatTextView.setPing(ping: Int, pingClassification: Classification) {

    text = if (ping > 0) {
        ping.toString()
    } else {
        context.getString(R.string.measurement_dash)
    }
    setCompoundDrawablesWithIntrinsicBounds(getPingClassificationIcon(pingClassification), 0, 0, 0)
}

/**
 * A binding adapter that is used for show download ping with classification icon in results
 */
@BindingAdapter("pingDouble", "pingClassification", requireAll = true)
fun AppCompatTextView.setPing(ping: Double, pingClassification: Classification) {

    text = if (ping > 0) {
        ping.toInt().toString()
    } else {
        context.getString(R.string.measurement_dash)
    }
    setCompoundDrawablesWithIntrinsicBounds(getPingClassificationIcon(pingClassification), 0, 0, 0)
}

fun getPingClassificationIcon(pingClassification: Classification): Int {
    return when (pingClassification) {
        Classification.NONE -> {
            R.drawable.ic_small_ping_gray
        }
        Classification.BAD -> {
            R.drawable.ic_small_ping_red
        }
        Classification.NORMAL -> {
            R.drawable.ic_small_ping_yellow
        }
        Classification.GOOD -> {
            R.drawable.ic_small_ping_light_green
        }
        Classification.EXCELLENT -> {
            R.drawable.ic_small_ping_dark_green
        }
    }
}

/**
 * A binding adapter that is used for show signal strength with classification icon in result
 */
@BindingAdapter("signalStrength", "signalStrengthClassification", requireAll = true)
fun AppCompatTextView.setSignalStrength(signalStrength: Int?, speedDownloadClassification: Classification) {

    text = signalStrength?.toString() ?: context.getString(R.string.measurement_dash)
    setCompoundDrawablesWithIntrinsicBounds(

        when (speedDownloadClassification) {
            Classification.NONE -> {
                R.drawable.ic_small_wifi_gray
            }
            Classification.BAD -> {
                R.drawable.ic_small_wifi_red
            }
            Classification.NORMAL -> {
                R.drawable.ic_small_wifi_yellow
            }
            Classification.GOOD -> {
                R.drawable.ic_small_wifi_light_green
            }
            Classification.EXCELLENT -> {
                R.drawable.ic_small_wifi_dark_green
            }
        }, 0, 0, 0
    )
}