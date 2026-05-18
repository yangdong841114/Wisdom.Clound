package com.wisdom.clound.goods;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.wisdom.clound.R;

public class DetailsActivity extends AppCompatActivity {

    private ImageView ivDetailsImage;
    private TextView tvDetailsName, tvDetailsPrice, tvDetailsDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_details);

        // 初始化控件
        ivDetailsImage = findViewById(R.id.iv_details_image);
        tvDetailsName = findViewById(R.id.tv_details_name);
        tvDetailsPrice = findViewById(R.id.tv_details_price);
        tvDetailsDesc = findViewById(R.id.tv_details_desc);

        // 接收从HomeFragment传递的商品参数
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int goodsId = extras.getInt("goods_id");
            String goodsName = extras.getString("goods_name");
            String goodsImage = extras.getString("goods_image");
            double goodsPrice = extras.getDouble("goods_price");
            String goodsDesc = extras.getString("goods_description");

            // 填充数据到控件
            tvDetailsName.setText(goodsName);
            tvDetailsPrice.setText("¥" + String.format("%.2f", goodsPrice));
            tvDetailsDesc.setText(goodsDesc == null || goodsDesc.isEmpty() ? "暂无商品描述" : goodsDesc);

            // 加载商品图片（复用HomeFragment的loadImage方法）
            loadImage(goodsImage, ivDetailsImage);
        }
    }

    // 复用HomeFragment的图片加载方法（也可封装为工具类）
    private void loadImage(String imageUrl, ImageView imageView) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(imageUrl);
                java.io.InputStream is = url.openStream();
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);
                runOnUiThread(() -> imageView.setImageBitmap(bitmap));
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    // 图片加载失败显示默认图
                    imageView.setImageResource(R.mipmap.ic_launcher);
                });
            }
        }).start();
    }
}