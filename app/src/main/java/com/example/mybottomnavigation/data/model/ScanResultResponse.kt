package com.example.mybottomnavigation.data.model

data class ScanResultData(
    val kode_scan: String,
    val nama_pasien: String,
    val nama_unit: String,
    val status_obat: String,
    val jam_daftar: String,
    val tgl_kunj: String,
    val info: String
)
data class ScanResultResponse(
    val success: Boolean,
    val message: String,
    val option: Int,
    val data: ScanResultData?,
    val html: String,
)
