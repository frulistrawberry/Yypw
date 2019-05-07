package com.hbcx.user.ui.ticket

import android.view.LayoutInflater
import cn.sinata.xldutils.utils.SpanBuilder
import com.hbcx.user.R
import com.hbcx.user.network.HttpManager
import com.hbcx.user.ui.MainActivity
import com.hbcx.user.ui.TranslateStatusBarActivity
import com.hbcx.user.utils.request
import com.uuzuche.lib_zxing.activity.CodeUtils
import android.graphics.Bitmap
import cn.sinata.xldutils.gone
import cn.sinata.xldutils.utils.SPUtils
import cn.sinata.xldutils.utils.hideIdCard
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.MultiFormatWriter
import com.google.zxing.BarcodeFormat
import com.hbcx.user.utils.Const
import kotlinx.android.synthetic.main.activity_ticket_pay_success.*
import kotlinx.android.synthetic.main.item_passenger_info.view.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.startActivity


class TicketPaySuccessActivity : TranslateStatusBarActivity() {
    val id by lazy {
        intent.getIntExtra("id", 0)
    }
    val orderNum by lazy {
        intent.getStringExtra("orderNum")
    }

    override fun setContentView() = R.layout.activity_ticket_pay_success

    override fun initClick() {
        tv_go_home.setOnClickListener {
            startActivity<MainActivity>()
        }
        tv_see_detail.setOnClickListener {
            startActivity<TicketOrderDetailActivity>("id" to id,"isCreate" to 1)
            finish()
        }
    }

    override fun initView() {
        title = "完成支付"
        tv_tip_1.text = SpanBuilder("※ 验票码已保存在订单详情中。").color(this, 0, 1, R.color.color_tv_orange).build()
        val phone = SPUtils.instance().getString(Const.SERVICE_PHONE)
        tv_tip_2.text = SpanBuilder("※ 若有任何疑问请拨打客服热线：$phone")
                .color(this, 0, 1, R.color.color_tv_orange)
                .color(this, 16, 16+phone.length, R.color.black_text).build()
        tv_num.text = "验票码：${orderNum.substring(7)}"
        iv_qr_code.setImageBitmap(CodeUtils.createImage("YunYou:$id", dip(136), dip(136), null))
        getData()
    }

    private fun getData() {
        HttpManager.getPassengerList(id).request(this) { _, data ->
            data?.let {
                if (it.isNotEmpty()) {
                    it.forEach {
                        val view = LayoutInflater.from(this).inflate(R.layout.item_passenger_info, null)
                        view.tv_name.text = it.name
                        view.tv_id_card.text = it.idCard.hideIdCard()
                        if (it.elTicket == null) {
                            view.iv_ticket_code.gone()
                        } else
                            view.iv_ticket_code.setOnClickListener { _ ->
                                iv_qr_code.imageBitmap = createBarcode(it.elTicket, dip(136), dip(136))
                            }
                        ll_passenger.addView(view)
                    }
                }
            }
        }
    }

    private fun createBarcode(contents: String, desiredWidth: Int, desiredHeight: Int): Bitmap? {
        var resultBitmap: Bitmap? = null
        val barcodeFormat = BarcodeFormat.EAN_8

        resultBitmap = encodeAsBitmap(contents, barcodeFormat,
                desiredWidth, desiredHeight)

        return resultBitmap
    }


    private fun encodeAsBitmap(contents: String,
                               format: BarcodeFormat, desiredWidth: Int, desiredHeight: Int): Bitmap {
        val WHITE = -0x1
        val BLACK = -0x1000000

        val writer = MultiFormatWriter()
        var result: BitMatrix? = null
        try {
            result = writer.encode(contents, format, desiredWidth,
                    desiredHeight, null)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        val width = result!!.width
        val height = result.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (result.get(x, y)) BLACK else WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}