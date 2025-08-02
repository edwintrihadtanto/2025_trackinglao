package com.example.mybottomnavigation.data.model

data class ScanResultData(
    val kode_scan: String,
    val nama_pasien: String,
    val nama_unit: String,
    val status_obat: String,
    val jam_daftar: String
)

data class ScanResultResponse(
    val success: Boolean,
    val message: String,
    val data: ScanResultData?
)
