package cn.zjy.statusanimationview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    StatusAnimationView view;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (StatusAnimationView) findViewById(R.id.view);
//        view.setCircleRadius(70);
//        view.setCircleWidth(2);
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.starAnim();
            }
        });
    }
}
