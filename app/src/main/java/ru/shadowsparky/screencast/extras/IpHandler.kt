/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.extras

import ru.shadowsparky.screencast.extras.Constants.NOT_FOUND_IPV4
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Используется исключительно для получения уникального IP v4 адреса данного Android устройства.
 * @since v1.0.0
 * @author shadowsparky
 */
class IpHandler {

    /**
     * Получение IP V4 адреса Android устройства.
     *
     * @since v1.0.0
     * @author shadowsparky
     * @return Возвращает адрес ipv4 адрес мобильного устройства. Если не найдет адрес, то вернется [NOT_FOUND_IPV4]
     */
    fun getIpv4() : String {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val iface = interfaces.nextElement()
            if (iface.isLoopback || !iface.isUp)
                continue
            val addresses = iface.inetAddresses
            while (addresses.hasMoreElements()) {
                val addr = addresses.nextElement()
                if (addr is Inet4Address) {
                    return addr.hostAddress
                }
            }
        }
        return NOT_FOUND_IPV4
    }
}