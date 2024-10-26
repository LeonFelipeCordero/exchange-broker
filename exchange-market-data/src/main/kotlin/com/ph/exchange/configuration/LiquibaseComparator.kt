package com.ph.exchange.configuration

class LiquibaseComparator : Comparator<String> {
    override fun compare(o1: String?, o2: String?): Int {
        requireNotNull(o1)
        requireNotNull(o2)

        return o1.substringAfterLast("/").compareTo(o2.substringAfterLast("/"))
    }
}