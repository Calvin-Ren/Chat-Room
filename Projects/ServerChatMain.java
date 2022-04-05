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
public class ServerChatMain extends JFrame implements ActionListener {


    public static void main(String[] args) {
        new ServerChatMain();//initialize
    }

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

    // output stream
    private BufferedWriter bw = null;

    //creation
    public ServerChatMain(){
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
        this.setTitle("CTalk-Server");
        this.setSize(300,300);
        this.setLocation(300,300);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        jb.addActionListener(this);
        try{
            ServerSocket serversocket = new ServerSocket(7777);


            Socket socket = serversocket.accept();

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = null;

            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));



            while((line = br.readLine()) != null) {

                if (line.contains("[File_request]")) {
                    jta.append("A File will be sent to you" + System.lineSeparator());
                    jta.append("File Name: " + line.split("#")[1] + System.lineSeparator());
                    int port = 9999;
                    FileThread filethread = new FileThread(port);
                    filethread.start();
                    System.out.println("File Thread start");
                    jta.append("File Transfer succeeded! Saved in: \"/Users/calvin/ \" " + System.lineSeparator());
                    bw.append("File Transfer succeeded!");
                    bw.newLine();
                    bw.flush();
                }

                else jta.append(line + System.lineSeparator());
            }
            serversocket.close();

        } catch (Exception e2) {
            e2.printStackTrace();
        }

        // extends a interface


        jsend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame();

                JFileChooser sourceFileChooser = new JFileChooser(".");
                sourceFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int status = sourceFileChooser.showOpenDialog(frame);
                File sourceFile = new File(sourceFileChooser.getSelectedFile().getPath());

                jta.append("Send the file: <"  + sourceFile.getName() + ">\r\n");
                Socket socket2 = null;
                try {
                    socket2 = new Socket("127.0.0.1", 9999);
                    SendFileThread sendFile = new SendFileThread(frame, socket2, "Client", sourceFileChooser, status);
                    sendFile.start();


                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            }
        });



    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // get the text box's text
        String text = jtf.getText();
        // combine the sending messages
        text = "Server: " + text;

        // show the text in own window
        jta.append(text + System.lineSeparator());
        try {
            // send the message
            bw.write(text);
            bw.newLine();
            bw.flush();

            // clear the text
            jtf.setText("");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    static class FileThread extends Thread {
        private int port;
        public boolean flag = false;

        public FileThread(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            Socket socket = null;
            DataOutputStream dos = null;
            try {
                socket = new Socket("127.0.0.1", port);
                System.out.println("Client already chose file");
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                // get the name of file
                File file = new File("/Users/calvin/"+dis.readUTF());
                // get the size of file
                double totleLength = dis.readLong();
                dos = new DataOutputStream(new FileOutputStream(file));
                // start to receive file
                System.out.println("Starting trans: "+totleLength);
                int length=-1;
                byte[] buff= new byte[1024];
                double curLength = 0;
                while((length=dis.read(buff))>0){
                    dos.write(buff, 0, length);
                    curLength+=length;
                    System.out.println("Trans per: "+(curLength/totleLength*100)+"%");
                }
                dos.flush();
                System.out.println("Trans succeeded!");
                flag = true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // close OutputStream
                    if (dos != null) {
                        dos.close();
                    }
                    // close socket
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
    }

    static class File_Send_Thead extends Thread {
        private ServerSocket fileSS;
        private int port;

        public File_Send_Thead(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            DataInputStream dis = null;
            Socket socket = null;
            try {
                fileSS = new ServerSocket(port);
                socket = fileSS.accept();
                System.out.println("Server already chose file");
                // select the file for transfer
                File file = new File("/Users/calvin/IdeaProjects/test/copy-of-file.txt-2");
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