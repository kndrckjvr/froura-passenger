package com.froura.develo4.passenger;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class SearchActivity extends AppCompatActivity {

    private EditText searchET;
    private ImageView clearImgVw;
    private RecyclerView listRecVw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchET = findViewById(R.id.searchET);
        clearImgVw = findViewById(R.id.clearImgVw);
        listRecVw = findViewById(R.id.listRecVw);

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(i1 > 0) {
                    clearImgVw.setVisibility(View.VISIBLE);
                } else {
                    clearImgVw.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }
}
