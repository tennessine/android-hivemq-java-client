package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Mqtt3AsyncClient client = MqttClient.builder()
                .useMqttVersion3()
                .identifier("android233")
                .serverHost("mqttx.cn")
                .serverPort(1883)
                .buildAsync();

        connect(client);
    }

    private void connect(Mqtt3AsyncClient client) {
        client.connectWith()
                .simpleAuth()
                .username("")
                .password("".getBytes())
                .applySimpleAuth()
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        Log.e("mqtt:", "there's some error");
                    } else {
                        // setup subscribes or start publishing
                        Log.e("mqtt:", "connected");

                        subscribe(client);

                        publish(client);
                    }
                });
    }

    private void publish(Mqtt3AsyncClient client) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                client.publishWith()
                        .topic("test/topic")
                        .payload("hello world".getBytes())
                        .send()
                        .whenComplete((publish, throwable) -> {
                            if (throwable != null) {
                                // handle failure to publish
                                Log.e("mqtt:", "there's some error when publish");
                            } else {
                                // handle successful publish, e.g. logging or incrementing a metric
                                Log.e("mqtt:", "publish success");
                            }
                        });
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void subscribe(Mqtt3AsyncClient client) {
        client.subscribeWith()
                .topicFilter("test/topic")
                .callback(mqtt3Publish -> {
                    Log.e("mqtt:", mqtt3Publish.getPayloadAsBytes().toString());
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        // Handle failure to subscribe
                        Log.e("mqtt:", "there's some error");
                    } else {
                        // Handle successful subscription, e.g. logging or incrementing a metric
                        Log.e("mqtt:", "subscribed");
                    }
                });
    }
}