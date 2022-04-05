package Project_04;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


//1.define every blocks in frame
//2.initialize all blocks
public class ClientChatMain extends JFrame{
    public static void main(String[] args) {
        new ClientChatMain();//initialize
    }

    private ServerSocket serversocket;
    //text area
    private JTextArea jta;
    //scroll pane
    private JScrollPane jsp;
    //blank space
    private JPanel jp;
    //text box
    private JTextField jtf;

    //button
    private JButton jb;
    //file send button
    private JButton jsend;

    // define the output stream
    private BufferedWriter bw = null;


    //creation
    public ClientChatMain(){
        //initial all block
        jta = new JTextArea();
        //set the text uneditable
        jta.setEditable(false);
        //add the text to the scroll pane to scroll
        jsp = new JScrollPane(jta);
        //blank space
        jp = new JPanel();
        //text box
        jtf = new JTextField(10);
        //button
        jb = new JButton("Send");
        //file sending button
        jsend = new JButton("Send File");

        //Add buttons to blank space
        jp.add(jtf);
        jp.add(jb);
        jp.add(jsend);


        //add everything into window
        this.add(jsp, BorderLayout.CENTER); //put in the middle
        this.add(jp,BorderLayout.SOUTH); //put in the bottom
        this.add(jsend,BorderLayout.NORTH);

        // set everything visible
        this.setTitle("CTalk-Client");
        this.setSize(300,300);
        this.setLocation(300,300);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        jb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get the sending messages in text box
                String text = jtf.getText();
                String temp_line = text;

                // combine the input text
                text = "Client: " + text;
                // show the text in own window
                if (!text.contains("[File_request]")) jta.append(text + System.lineSeparator());
                else {
                    jta.append("File Transfer Start......" + System.lineSeparator());
                    jta.append("File Name:" + text.split("#")[1] + System.lineSeparator());
                }
                try {
                    if (temp_line.contains("[File_request]")) {
                        int port = 9999;
                        FileThread filethread = new FileThread(port);
                        filethread.start();
                        System.out.println("File Thread start" + System.lineSeparator());

                    }
                    // send the message
                    bw.append(text);
                    bw.newLine();
                    bw.flush();

                    // clear the text box
                    jtf.setText("");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            }
        });
        try {
            // create a server catcher
            Socket socket = new Socket("127.0.0.1", 7777);
            // catch input stream from socket
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // catch output stream from socket
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // get the input data in loop, combine them into text box
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains("File Transfer succeeded!")) {
                    jta.append("File Transfer succeeded!" + System.lineSeparator());
                }
                else jta.append(line + System.lineSeparator());
            }

            // close the socket channel
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }




        jsend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                System.out.println("test_back");
                JFrame frame = new JFrame();

                JFileChooser sourceFileChooser = new JFileChooser(".");
                sourceFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int status = sourceFileChooser.showOpenDialog(frame);
                File sourceFile = new File(sourceFileChooser.getSelectedFile().getPath());

                jta.append("Send the file: <"  + sourceFile.getName() + ">\r\n");
                Socket socket2 = null;
                try {
                    socket2 = new Socket("127.0.0.1", 9999);
                    SendFileThread sendFile = new SendFileThread(frame, socket2, "Server", sourceFileChooser, status);
                    sendFile.start();


                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }


            }
        });

    }
    static class FileThread extends Thread {

        private ServerSocket fileSS;
        private int port;

        public FileThread(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            DataInputStream dis = null;
            Socket socket = null;
            try {
                fileSS = new ServerSocket(port);
                socket = fileSS.accept();
                System.out.println("Client already chose file");
                // select the file for transfer
                File file = new File("/Users/calvin/IdeaProjects/test/copy-of-file.txt");
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(new FileInputStream(file));
                // get the name of file
                dos.writeUTF(file.getName());
                dos.flush();
                // get the size of file
                dos.writeLong(file.length());
                dos.flush();

                System.out.println("Starting trans(size: "+file.getTotalSpace()+")");

                int length = -1;// get the size of file
                byte[] buff = new byte[1024];

                while ((length = dis.read(buff)) > 0) {
                    dos.write(buff, 0, length);
                    dos.flush();
                }
                System.out.println("Trans succeeded!");

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // close InputStream
                    if (dis != null) {
                        dis.close();
                    }
                    // close the socket port
                    if (socket != null) {
                        socket.close();
                    }
                    // close socket
                    if (fileSS != null) {
                        fileSS.close();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
    }


}