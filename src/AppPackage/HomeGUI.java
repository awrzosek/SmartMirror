
package AppPackage;


import com.google.gson.*;
import com.google.gson.reflect.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

class weather {
    int id;
    String main;
    String description;
    String icon;    
}

class notes {
    int note_ID;
    String title;
    String note;
}

public class HomeGUI extends javax.swing.JFrame {

    public HomeGUI() {
        initComponents();
        //fullscreen options
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // get notes every few seconds
        Runnable helloRunnable = new Runnable() {
            public void run() {
                getNotes();
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, 0, 3, TimeUnit.SECONDS);

        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        getWeather();
        
        // wyłączenie widoczności kursora
        // Transparent 16 x 16 pixel cursor image.
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        // Create a new blank cursor.
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
        cursorImg, new Point(0, 0), "blank cursor");

        // Set the blank cursor to the JFrame.
        this.getContentPane().setCursor(blankCursor);
        //-------------------------------------------------------------

        // nowy wątek dla wyświetlania zegara i daty
        new Thread() {
            public void run() {
                while(true){
                    Calendar cal = new GregorianCalendar();
                    int hh = cal.get(Calendar.HOUR_OF_DAY);
                    int mm = cal.get(Calendar.MINUTE);
                    int ss = cal.get(Calendar.SECOND);
                    String time = hh + ":" + String.format("%02d", mm) + ":" + String.format("%02d", ss);
                    clockLabel.setText(time);
                    
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    int Month = cal.get(Calendar.MONTH);
                    int year = cal.get(Calendar.YEAR);
                    String month = "";
                    switch(Month){
                        case 0 : month = "Styczeń"; break;
                        case 1 : month = "Luty"; break;
                        case 2 : month = "Marzec"; break;
                        case 3 : month = "Kwiecień"; break;
                        case 4 : month = "Maj"; break;
                        case 5 : month = "Czerwiec"; break;
                        case 6 : month = "Lipiec"; break;
                        case 7 : month = "Sierpień"; break;
                        case 8 : month = "Wrzesień"; break;
                        case 9 : month = "Październik"; break;
                        case 10 : month = "Listopad"; break;
                        case 11 : month = "Grudzień"; break;
                                        
                    }
                    String date = day + " " + month + " " + year;
                    dateLabel.setText(date);
                }
            }
        }.start();
    }
    
    public void getImg(String imgName){
        BufferedImage img = null;
        String filePath = "imgs/" + imgName + ".png";
        try{
            img = ImageIO.read(new FileInputStream(filePath));
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        imageLabel.setIcon(new ImageIcon(img));
    }
    
    public static Map<String, Object> jsonToMap(String str) {
        Map<String, Object> map = new Gson().fromJson(
            str, new TypeToken<HashMap<String, Object>>() {}.getType()
        );
        return map;
    }
    
    private void getNotes(){
        String urlString = "http://smnotes.azurewebsites.net/api/notes";
        try{
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while((line = rd.readLine()) != null){
                    result.append(line);
                }
            }
            JsonParser jsonParser = new JsonParser();
            JsonArray arrayFromString = jsonParser.parse(result.toString()).getAsJsonArray();
            
            java.lang.reflect.Type listType = new TypeToken<ArrayList<notes>>(){}.getType();
            ArrayList<notes> notesList = new Gson().fromJson(arrayFromString, listType);
            DefaultListModel<notes> listModel = new DefaultListModel<>();
            for (int i = 0; i < notesList.size(); i++)
            {
                listModel.addElement(notesList.get(i));
            }
            jList1.setModel(listModel);
            jList1.setCellRenderer(new NotesRenderer());
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public class NotesRenderer extends JPanel implements ListCellRenderer<notes> {

        private JLabel titleLabel = new JLabel();
        private JLabel noteLabel = new JLabel();
        
        public NotesRenderer() {
            setLayout(new BorderLayout(5,5));
            
            JPanel panelText = new JPanel(new GridLayout(0,1));
            panelText.setBackground(Color.BLACK);
            panelText.add(titleLabel);
            panelText.add(noteLabel);
            add(panelText,BorderLayout.CENTER);
        }
        @Override
        public Component getListCellRendererComponent(JList<? extends notes> list, notes value, int index, boolean isSelected, boolean cellHasFocus) {
            titleLabel.setText("<html><body style='width: 200 px'>" + value.title);
            titleLabel.setForeground(Color.white);
            titleLabel.setBorder(new EmptyBorder(10,5,0,5));
            titleLabel.setFont(new Font("Times New Roman",Font.PLAIN,28));
            noteLabel.setText("<html><body style='width: 200 px'>" + value.note);
            noteLabel.setForeground(Color.white);
            noteLabel.setBorder(new EmptyBorder(0,0,5,0));
            noteLabel.setFont(new Font("Times New Roman",Font.PLAIN,18));
            return this;
        }
        
    }
    

    private void getWeather(){
        String API_KEY = "0da5e3056a8449787c3c4eea700e733e";
        String LOCATION_ID = "3100946";
        String urlString = "http://api.openweathermap.org/data/2.5/weather?id=" + LOCATION_ID + "&APPID=" + API_KEY + "&units=metric";
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while((line = rd.readLine()) != null) {
                    result .append(line);
                }
            }
            
            Map<String, Object> respMap = jsonToMap(result.toString());
            
            weather[] weatherArray = new Gson().fromJson(respMap.get("weather").toString().replaceAll("\\s+",""), weather[].class);
            getImg(weatherArray[0].description);
            switch(weatherArray[0].description){
                case "clearsky" : weatherArray[0].description = "Czyste niebo"; break;
                case "fewclouds" : weatherArray[0].description = "Częściowo pochmurnie"; break;
                case "scatteredclouds" : weatherArray[0].description = "Pochmurnie"; break;
                case "brokenclouds" : weatherArray[0].description = "Pochmurnie"; break;
                case "showerrain" : weatherArray[0].description = "Ulewne deszcze"; break;
                case "rain" : weatherArray[0].description = "Deszczowo"; break;
                case "thunderstorm" : weatherArray[0].description = "Burzowo"; break;
                case "snow" : weatherArray[0].description = "Śnieg"; break;
                case "mist" : weatherArray[0].description = "Mgła"; break;  
            }
            
            Map<String, Object> mainMap = jsonToMap(respMap.get("main").toString());
            Map<String, Object> windMap = jsonToMap(respMap.get("wind").toString());
            
            tempLabel.setText(mainMap.get("temp") + "\u00b0C");
            pressureLabel.setText(mainMap.get("pressure") + " hPa");
            cityLabel.setText(respMap.get("name").toString());
            windSpeedLabel.setText(windMap.get("speed") + " km/h");
            conditionLabel.setText(weatherArray[0].description);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        tempLabel = new javax.swing.JLabel();
        pressureLabel = new javax.swing.JLabel();
        cityLabel = new javax.swing.JLabel();
        windSpeedLabel = new javax.swing.JLabel();
        clockLabel = new javax.swing.JLabel();
        dateLabel = new javax.swing.JLabel();
        conditionLabel = new javax.swing.JLabel();
        imageLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setBackground(new java.awt.Color(0, 0, 0));
        setForeground(new java.awt.Color(0, 0, 0));
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));
        jPanel1.setPreferredSize(new java.awt.Dimension(768, 1280));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tempLabel.setFont(new java.awt.Font("Times New Roman", 0, 56)); // NOI18N
        tempLabel.setForeground(new java.awt.Color(255, 255, 255));
        tempLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        tempLabel.setText("15 C");
        tempLabel.setMaximumSize(new java.awt.Dimension(110, 66));
        tempLabel.setMinimumSize(new java.awt.Dimension(110, 66));
        tempLabel.setPreferredSize(new java.awt.Dimension(110, 66));
        jPanel1.add(tempLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(326, 190, 230, -1));

        pressureLabel.setBackground(new java.awt.Color(0, 0, 0));
        pressureLabel.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        pressureLabel.setForeground(new java.awt.Color(255, 255, 255));
        pressureLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pressureLabel.setText("1000hPa");
        jPanel1.add(pressureLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 250, 176, -1));

        cityLabel.setBackground(new java.awt.Color(0, 0, 0));
        cityLabel.setFont(new java.awt.Font("Times New Roman", 0, 24)); // NOI18N
        cityLabel.setForeground(new java.awt.Color(255, 255, 255));
        cityLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cityLabel.setText("Częstochowa");
        jPanel1.add(cityLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 110, 176, 38));

        windSpeedLabel.setBackground(new java.awt.Color(0, 0, 0));
        windSpeedLabel.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        windSpeedLabel.setForeground(new java.awt.Color(255, 255, 255));
        windSpeedLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        windSpeedLabel.setText("10 km/h");
        jPanel1.add(windSpeedLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 270, 176, -1));

        clockLabel.setFont(new java.awt.Font("Times New Roman", 0, 42)); // NOI18N
        clockLabel.setForeground(new java.awt.Color(255, 255, 255));
        clockLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        clockLabel.setText("12:44:11");
        jPanel1.add(clockLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 70, 196, -1));

        dateLabel.setFont(new java.awt.Font("Times New Roman", 0, 24)); // NOI18N
        dateLabel.setForeground(new java.awt.Color(255, 255, 255));
        dateLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        dateLabel.setText("12 Październik 2018");
        jPanel1.add(dateLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(516, 50, 240, -1));

        conditionLabel.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        conditionLabel.setForeground(new java.awt.Color(255, 255, 255));
        conditionLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        conditionLabel.setText("Czyste niebo");
        jPanel1.add(conditionLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(291, 170, 260, -1));

        imageLabel.setMaximumSize(new java.awt.Dimension(128, 128));
        imageLabel.setMinimumSize(new java.awt.Dimension(152, 128));
        imageLabel.setPreferredSize(new java.awt.Dimension(128, 152));
        jPanel1.add(imageLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 10, -1, -1));

        jScrollPane1.setBackground(new java.awt.Color(0, 0, 0));
        jScrollPane1.setBorder(null);
        jScrollPane1.setForeground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane1.setHorizontalScrollBar(null);

        jList1.setBackground(new java.awt.Color(0, 0, 0));
        jList1.setForeground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setViewportView(jList1);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 290, 300, 960));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HomeGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HomeGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HomeGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HomeGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HomeGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cityLabel;
    private javax.swing.JLabel clockLabel;
    private javax.swing.JLabel conditionLabel;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JLabel imageLabel;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel pressureLabel;
    private javax.swing.JLabel tempLabel;
    private javax.swing.JLabel windSpeedLabel;
    // End of variables declaration//GEN-END:variables
}
