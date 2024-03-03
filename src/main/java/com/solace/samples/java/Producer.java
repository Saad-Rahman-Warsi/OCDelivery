package com.solace.samples.java;

import com.solace.messaging.MessagingService;
import com.solace.messaging.config.SolaceProperties;
import com.solace.messaging.config.profile.ConfigurationProfile;
import com.solace.messaging.publisher.DirectMessagePublisher;
import com.solace.messaging.publisher.OutboundMessage;
import com.solace.messaging.publisher.OutboundMessageBuilder;
import com.solace.messaging.receiver.DirectMessageReceiver;
import com.solace.messaging.receiver.MessageReceiver;
import com.solace.messaging.resources.Topic;
import com.solace.messaging.resources.TopicSubscription;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Scanner;



import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Producer {
    int mode=1;
    private JFrame mainFrame;
    private JLabel headerLabel;
    private JPanel controlPanel;
    private JPanel optPanel;

    private JPanel arrInfoPanel;
    JTextField aIPNoText;
    JTextField aIMsgText;

    private JPanel regPktPanel;
    JTextField rPNameText;
    JTextField rPBSText;

    private JPanel delverPanel;

    public Producer(){
        prepareGUI();
    }

    public static void main(String[] args){
        Producer prod = new Producer();
        prod.showEventDemo();
    }

    private void prepareGUI(){
        mainFrame = new JFrame("Java SWING Examples");
        mainFrame.setSize(500,600);
        mainFrame.setLayout(new GridLayout(5, 1));

        headerLabel = new JLabel("",JLabel.CENTER );

        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        prepArrInfoPanel();
        prepDelverPanel();
        prepRegPktPanel();

        optPanel = regPktPanel;
        mainFrame.add(headerLabel);
        mainFrame.add(controlPanel);
        mainFrame.add(optPanel);
        mainFrame.setVisible(true);
    }

    private void showEventDemo(){
        headerLabel.setText("Bus Stop Courier Service");

        JButton okButton = new JButton("Register Packet");
        JButton submitButton = new JButton("Send Arrival Message");
        JButton cancelButton = new JButton("Submit");

        okButton.setActionCommand("OK");
        submitButton.setActionCommand("Submit");
        cancelButton.setActionCommand("Cancel");

        okButton.addActionListener(new ButtonClickListener());
        submitButton.addActionListener(new ButtonClickListener());
        cancelButton.addActionListener(new ButtonClickListener());

        controlPanel.add(okButton);
        controlPanel.add(submitButton);
        controlPanel.add(cancelButton);

        mainFrame.setVisible(true);
    }

    private class ButtonClickListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if( command.equals( "OK" ))  {
                mode=1;
                optPanel.setVisible(false);
                mainFrame.remove(optPanel);
                optPanel = regPktPanel;
                mainFrame.add(optPanel);
                optPanel.setVisible(true);
            }
            else if( command.equals( "Submit" ) )  {
                mode=2;
                optPanel.setVisible(false);
                mainFrame.remove(optPanel);
                optPanel = arrInfoPanel;
                mainFrame.add(optPanel);
                optPanel.setVisible(true);
            }
            else {
                // send to consumer
                ;
                try {
                    if (mode==2) {
                        produce(aIPNoText.getText() + "   " + aIMsgText.getText());
                    }
                    else {
                        produce(rPNameText.getText() + "   " + rPBSText.getText());
                    }
                }
                catch (Exception ep)
                {
                    ;
                }
            }
        }
    }

    private void prepArrInfoPanel() {
        arrInfoPanel = new JPanel();
        arrInfoPanel.setSize(10, 10);
        arrInfoPanel.setLayout(new GridLayout(2, 2));
        JLabel aIPNoLabel = new JLabel("Packet No :");
        aIPNoText = new JTextField("                  ");
        JLabel aIMsgLabel = new JLabel("Arrival Message :");
        aIMsgText = new JTextField("                  ");
        arrInfoPanel.add(aIPNoLabel);
        arrInfoPanel.add(aIPNoText);
        arrInfoPanel.add(aIMsgLabel);
        arrInfoPanel.add(aIMsgText);
    }

    private void prepDelverPanel() {
        delverPanel = new JPanel();
        delverPanel.setSize(10, 10);
        delverPanel.setLayout(new GridLayout(1, 2));
        JLabel delPNoLabel = new JLabel("Packet No :");
        JTextField delPNoText = new JTextField("                  ");
        delverPanel.add(delPNoLabel);
        delverPanel.add(delPNoText);
    }

    private void prepRegPktPanel() {
        regPktPanel = new JPanel();
        regPktPanel.setSize(10, 10);
        regPktPanel.setLayout(new GridLayout(2, 2));
        JLabel rPNameLabel = new JLabel("Name :");
        rPNameText = new JTextField("                  ");
        JLabel rPBSLabel = new JLabel("Bus Stop No :");
        rPBSText = new JTextField("                  ");
        regPktPanel.add(rPNameLabel);
        regPktPanel.add(rPNameText);
        regPktPanel.add(rPBSLabel);
        regPktPanel.add(rPBSText);
    }
    private static final String SAMPLE_NAME = Producer.class.getSimpleName();
    private static final String TOPIC_PREFIX = "solace/samples/";  // used as the topic "root"
    private static final String API = "Java";
    private static volatile boolean isShutdown = false;           // are we done yet?

    /** Simple application for doing pub/sub. */
    public static void produce(String tt) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String uniqueName = "";


        System.out.println(API + " " + SAMPLE_NAME + " initializing...");
        final Properties properties = new Properties();
        properties.setProperty(SolaceProperties.TransportLayerProperties.HOST, "tcps://mr-connection-f37s6kixqzn.messaging.solace.cloud:55443");          // host:port
        properties.setProperty(SolaceProperties.ServiceProperties.VPN_NAME,  "octransposervice");     // message-vpn
        properties.setProperty(SolaceProperties.AuthenticationProperties.SCHEME_BASIC_USER_NAME, "solace-cloud-client");      // client-username
        properties.setProperty(SolaceProperties.AuthenticationProperties.SCHEME_BASIC_PASSWORD, "tb64dco6u6j41vlibiqoqqimfs");  // client-password


        properties.setProperty(SolaceProperties.ServiceProperties.RECEIVER_DIRECT_SUBSCRIPTION_REAPPLY, "true");  // subscribe Direct subs after reconnect

        final MessagingService messagingService = MessagingService.builder(ConfigurationProfile.V1)
                .fromProperties(properties).build().connect();  // blocking connect to the broker

        // create and start the publisher
        final DirectMessagePublisher publisher = messagingService.createDirectMessagePublisherBuilder()
                .onBackPressureWait(1).build().start();





        System.out.printf("%nConnected and subscribed. Ready to publish. Press [ENTER] to quit.%n");
        System.out.printf(" ~ Run this sample twice splitscreen to see true publish-subscribe. ~%n%n");

        OutboundMessageBuilder messageBuilder = messagingService.messageBuilder();
        char c='y';

            Scanner Sc=new Scanner(System.in);
            try {

                uniqueName=tt;
                // payload is our "hello world" message from you!
                OutboundMessage message = messageBuilder.build(String.format("Hello World from %s!", uniqueName));
                // make a dynamic topic: solace/samples/java/hello/[uniqueName]
                String topicString = "topic";
                System.out.println ("enter message");

                System.out.printf(">> Calling send() on %s%n", topicString);
                publisher.publish(message, Topic.of(topicString));
            } catch (RuntimeException e) {
                System.out.printf("### Exception caught during producer.send(): %s%n", e);
                isShutdown = true;
            }
            System.out.println("press y to continue");
            //c=Sc.nextLine().charAt(0);


        isShutdown = true;
        publisher.terminate(500);
        messagingService.disconnect();

    }
}


