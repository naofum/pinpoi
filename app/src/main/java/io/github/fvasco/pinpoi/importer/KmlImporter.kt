package io.github.fvasco.pinpoi.importer

import io.github.fvasco.pinpoi.util.Coordinates
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.IOException
import java.net.URL

/**
 * KML importer

 * @author Francesco Vasco
 */
class KmlImporter : AbstractXmlImporter(), AnkoLogger {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var coordinateCount: Int = 0

    override fun handleStartTag() {
        when (tag) {
            "Placemark" -> {
                newPlacemark()
                latitude = 0.0
                longitude = 0.0
                coordinateCount = 0
            }
        }
    }

    @Throws(IOException::class)
    override fun handleEndTag() {
        if (placemark == null) {
            if ("href" == tag && checkCurrentPath("kml", "Document", "NetworkLink", "Url")) {
                val href = text
                val delegateImporter = ImporterFacade.createImporter(href, fileFormatFilter)
                info("NetworkLink href $href importer $delegateImporter")
                delegateImporter?.let { delegateImporter ->
                    URL(href).openStream().use { inputStream ->
                        delegateImporter.configureFrom(this)
                        delegateImporter.importPlacemarks(inputStream)
                    }
                }
            }
        } else {
            val p = placemark ?: return
            when (tag) {
                "Placemark" -> {
                    if (coordinateCount > 0) {
                        // set placemark to center
                        p.coordinates = Coordinates((latitude / coordinateCount.toDouble()).toFloat(),
                                (longitude / coordinateCount.toDouble()).toFloat())
                    }
                    importPlacemark()
                }
                "name" -> p.name = text
                "description" -> p.description = text
                "coordinates" -> // read multiple lines if present (point, line, polygon)
                    for (line in text.trim { it <= ' ' }.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }) {
                        // format: longitude, latitute, altitude
                        val coordinates = line.split(",".toRegex(), 3)
                        longitude += java.lang.Double.parseDouble(coordinates[0])
                        latitude += java.lang.Double.parseDouble(coordinates[1])
                        ++coordinateCount
                    }
            }
        }
    }
}