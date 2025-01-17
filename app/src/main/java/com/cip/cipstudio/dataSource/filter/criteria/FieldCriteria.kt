package com.cip.cipstudio.dataSource.filter.criteria

class FieldCriteria(private val field: FilterFieldEnum): Criteria {
    private var values: List<String> = ArrayList()

    constructor(field: FilterFieldEnum, values: List<String>) : this(field) {
        this.values = values
    }

    override fun buildQuery(): String {
        return if (values.isNotEmpty())
            "${field.getFilterFieldIGDBName()} = (${values.joinToString(",")}) & ${field.getFieldControl()}"
        else
            ""
    }

    override fun isEmpty() : Boolean {
        return values.isEmpty()
    }
}
