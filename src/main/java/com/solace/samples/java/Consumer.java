/*
 * Copyright 2021-2023 Solace Corporation. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package com.solace.samples.java;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import com.solace.messaging.MessagingService;
import com.solace.messaging.config.SolaceProperties.AuthenticationProperties;
import com.solace.messaging.config.SolaceProperties.ServiceProperties;
import com.solace.messaging.config.SolaceProperties.TransportLayerProperties;
import com.solace.messaging.config.profile.ConfigurationProfile;
import com.solace.messaging.publisher.DirectMessagePublisher;
import com.solace.messaging.publisher.OutboundMessageBuilder;
import com.solace.messaging.receiver.DirectMessageReceiver;
import com.solace.messaging.receiver.MessageReceiver.MessageHandler;
import com.solace.messaging.resources.TopicSubscription;

/**
 * This simple introductory sample shows an application that both publishes and subscribes.
 */
public class Consumer {

    private JFrame mainFrame;
    private JLabel headerLabel;
    public static JTextArea msgArea;
    
    private static final String SAMPLE_NAME = Consumer.class.getSimpleName();
    private static final String TOPIC_PREFIX = "solace/samples/";  // used as the topic "root"
    private static final String API = "Java";
    private static volatile boolean isShutdown = false;           // are we done yet?

    /** Simple application for doing pub/sub. */

    //package com.tutorialspoint.gui;






        public Consumer(){
            prepareGUI();
        }

        public static void main(String[] args)throws IOException{
            Consumer cons = new Consumer();
            cons.showEventDemo();
             /*if (args.length < 3) {  // Check command line arguments
            System.out.printf("Usage: %s <host:port> <message-vpn> <client-username> [password]%n", SAMPLE_NAME);
            System.out.printf("  e.g. %s localhost default default%n%n", SAMPLE_NAME);
            System.exit(-1);
        }*/
            // User prompt, what is your name??, to u+-+se in the topic
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String uniqueName = "";


            System.out.println(API + " " + SAMPLE_NAME + " initializing...");
            final Properties properties = new Properties();
            properties.setProperty(TransportLayerProperties.HOST, "tcps://mr-connection-f37s6kixqzn.messaging.solace.cloud:55443");          // host:port
            properties.setProperty(ServiceProperties.VPN_NAME,  "octransposervice");     // message-vpn
            properties.setProperty(AuthenticationProperties.SCHEME_BASIC_USER_NAME, "solace-cloud-client");      // client-username
            properties.setProperty(AuthenticationProperties.SCHEME_BASIC_PASSWORD, "tb64dco6u6j41vlibiqoqqimfs");  // client-password

            properties.setProperty(ServiceProperties.RECEIVER_DIRECT_SUBSCRIPTION_REAPPLY, "true");  // subscribe Direct subs after reconnect

            final MessagingService messagingService = MessagingService.builder(ConfigurationProfile.V1)
                    .fromProperties(properties).build().connect();  // blocking connect to the broker

            // create and start the publisher
            final DirectMessagePublisher publisher = messagingService.createDirectMessagePublisherBuilder()
                    .onBackPressureWait(1).build().start();

            // create and start the subscriber
            final DirectMessageReceiver receiver = messagingService.createDirectMessageReceiverBuilder()
                    .withSubscriptions(TopicSubscription.of("topic")).build().start();
            final MessageHandler messageHandler = (inboundMessage) -> {
                System.out.printf("vvv RECEIVED A MESSAGE vvv%n%s===%n",inboundMessage.dump());// just print
                String a=msgArea.getText();
                String c=inboundMessage.getPayloadAsString();
                c=c.substring(16,c.length()-1);
                a=a+'\n'+c;
                msgArea.setText(a);
            };
            receiver.receiveAsync(messageHandler);

            System.out.printf("%nConnected and subscribed. Ready to publish. Press [ENTER] to quit.%n");
            System.out.printf(" ~ Run this sample twice splitscreen to see true publish-subscribe. ~%n%n");

            OutboundMessageBuilder messageBuilder = messagingService.messageBuilder();
            while (System.in.available() == 0 && !isShutdown) {  // loop now, just use main thread
            /*try {
                Thread.sleep(5000);  // take a pause
                // payload is our "hello world" message from you!
                OutboundMessage message = messageBuilder.build(String.format("Hello World from %s!",uniqueName));
                // make a dynamic topic: solace/samples/java/hello/[uniqueName]
                String topicString = "topic";
                System.out.printf(">> Calling send() on %s%n",topicString);
                publisher.publish(message, Topic.of(topicString));
            } catch (RuntimeException e) {
                System.out.printf("### Exception caught during producer.send(): %s%n",e);
                isShutdown = true;
            } catch (InterruptedException e) {
                // Thread.sleep() interrupted... probably getting shut down
            }*/
            }
            isShutdown = true;
            //publisher.terminate(500);
            //receiver.terminate(500);
            messagingService.disconnect();
            System.out.println("Main thread quitting.");
        }

        private void prepareGUI(){
            mainFrame = new JFrame("Customer Window");
            mainFrame.setSize(500,600);
            mainFrame.setLayout(new GridLayout(2, 1));

            headerLabel = new JLabel("",JLabel.CENTER );

            mainFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent windowEvent){
                    System.exit(0);
                }
            });
            msgArea = new JTextArea();

            mainFrame.add(headerLabel);
            mainFrame.add(msgArea);
            mainFrame.setVisible(true);
        }

        private void showEventDemo(){
            headerLabel.setText("Customer Window");

            mainFrame.setVisible(true);
        }

    }

