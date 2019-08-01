/**
 * @(#)ActSetReadIDNum.java 2016/5/3 0003
 * 版权所有 (c) 2008-2016 广州市森锐科技股份有限公司
 */
package com.sunrise.readerdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 *
 *
 * @author Leo
 * @version 1.0 2016/5/3 0003
 * @since JDK1.6
 */
public class ActSetReadIDNum extends Activity {

    private EditText etSetReadNum;
    private Button bntSetReadNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_read_num);

        etSetReadNum = (EditText) findViewById(R.id.et_set_read_num);
        bntSetReadNum = (Button) findViewById(R.id.bnt_set_read_num);

        bntSetReadNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                int num = Integer.valueOf(etSetReadNum.getText().toString());
                intent.putExtra("readingNum", num);
                setResult(100, intent);
                finish();
            }
        });
    }
}
