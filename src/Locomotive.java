import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/*
 * This class creates an application with an input form where a user can enter the first 11 digits of a locomotive serial number
 * and then the user can obtain the 12th digit of the locomotive number, which represents the check digit of the locomotive.
 * This application is developed using JavaFX 8.
 */

public class Locomotive extends Application {

  /* maximum input length */
  final int maxLength = 11;

  /* variable used for knowing when the application displayed a message after shortening a too long input given by the user */
  boolean lenghtLimitMessageDislayed = false;

  /* variable used for displaying the result */
  Label result = new Label();

  /*
   * This method is used for validating user input.
   * Each input field can contain 11 digits (0-9).
   */
  private boolean validateValue(String inputString){

    Pattern p = Pattern.compile("[0-9]{11}");
    Matcher m = p.matcher(inputString);
    boolean b = m.matches();
    return b;

  }

  /*
   * This method calculates the check digit of the locomotive.
   */
  private int generateCheckDigit(String serialNumber){

    /* for defensive purpose, we make sure that the input is valid, before proceeding further */
    if(validateValue(serialNumber)==false)
      return -1;

    int checkDigit = -1;

    /* this array will contain each digit from the input string */
    int [] mainNumber = new int[11];

    int size = serialNumber.length();
    int i; /* used as iterator */

    /* extract the 11 characters from the input string and convert them to digits: */
    for (i=0;i<size;i++)
      mainNumber[i]=Character.getNumericValue(serialNumber.charAt(i));

    /* this mask will be used when calculating the check digit */
    int[] mask = new int[]{2,1,2,1,2,1,2,1,2,1,2};

    /* 
     * each element in this array will contain the multiplication of 
     * each digit from the locomotive number by each digit from the mask:
     */
    int[] mainNumberWithMaskApplied = new int[11];
    for (i=0;i<size;i++)
      mainNumberWithMaskApplied[i]=mainNumber[i]*mask[i];

    /*
     * We now need to add all the digits from all the numbers resulted after the multiplication.
     * Since by multiplying 2 digits, sometimes the result could have 2 digits, to avoid dividing numbers 
     * by 10 to extract each digit, we will concatenate all the elements in a larger String, then iterate
     * through the string, thus reading digit by digit (as char):
     */
    String concatenatedSequence="";
    for (i=0;i<size;i++)
      concatenatedSequence = concatenatedSequence + mainNumberWithMaskApplied[i];

    /* add all the digits from the obtained string */
    int finalSum=0;
    for (i=0;i<concatenatedSequence.length();i++)			
      finalSum = finalSum + Character.getNumericValue(concatenatedSequence.charAt(i));

    /* get the last digit of the sum */
    int lastDigit = finalSum % 10;

    /* set the check digit */
    if (lastDigit == 0)
      checkDigit = 0;
    else
      checkDigit = 10 - lastDigit;

    return checkDigit;
  }

  /*
   * Since JavaFX 8 does not come with a built-in method to limit the number of characters typed in a TextField,
   * this method will be used as a workaround.
   * If the input exceeds the maximum allowed length, it is shortened to match that length.
   */
  private void limitSize(TextField tf, int maximumLength){

    if ( tf.getText().length() > maximumLength ) {

      String resizedText=tf.getText(0, maximumLength);
      /*
       * Since "limitSize" method will be called inside a change listener, modifying the input field by "limitSize" method will result in triggering 
       * again the same listener, which will re-call the "limitSize" method, thus ending up with another "limitSize" method starting to execute
       * before the first one ended, which will lead to undesired behavior, such as incorrect caret positioning or exceptions thrown in the background. 
       * To prevent this, we use "Platform.runLater" method.
       */
      Platform.runLater(() -> {

    	int position = tf.getCaretPosition();
        tf.setText(resizedText);
        tf.positionCaret(position);

        /* 
         * display a message that informs there is a length limit, 
         * so that the user would know why the input was shortened
         */
        showMessage("Input field is limited to "+maximumLength+" characters.");

        /* mark that the input has been automatically shortened and that a message about this has been displayed */
        lenghtLimitMessageDislayed = true;

      });

    }

    else{

      /* it means that the input does not exceed the length limit */
      if (lenghtLimitMessageDislayed == true){

        /* 
         * it means that the user's input has previously been shortened, but now there is no case for this,
         * so the message about the previous shortening should no longer be displayed
         */
        showMessage("");
        lenghtLimitMessageDislayed = false;

      }

    }

  }

  public static void main(String[] args){
    Application.launch(args);
  }

  @Override
  public void start(Stage stage){

    String textFont = "Arial";

    Image locomotiveImage;
    ImageView locomotiveImageView = new ImageView();

    /*
     * Although loading the image does not generate checked exceptions, we put the code for it
     * in a try-catch block, so that the application could run even if no picture was found.
     */
    try{
      locomotiveImage = new Image(getClass().getResourceAsStream("EA060.jpg"));
      locomotiveImageView.setImage(locomotiveImage);
    }
    catch(Exception e){
      /* do nothing here; the application will run without showing the image */
    }

    result.setFont(Font.font(textFont, FontWeight.BOLD, 12));

    Label inputMessage = new Label ("Enter locomotive serial number:");
    inputMessage.setFont(Font.font(textFont, 12));

    /* creating the input TextField for the serial number */
    TextField serialNumber = new TextField();
    serialNumber.setFont(Font.font(textFont, FontWeight.BOLD, 22));
    serialNumber.setMaxWidth(170);

    /* adding a listener, to be called whenever the text for the serial number changes */
    serialNumber.textProperty().addListener((observable,oldValue,newValue)->{

      /* the input is not allowed to exceed a certain size */
      limitSize(serialNumber, maxLength);

    });

    /* creating a Submit button, with G as its mnemonic */
    Button submitButton = new Button("_Generate");

    /* setting the Submit button as default button, to be called if the user presses Enter key */
    submitButton.setDefaultButton(true);

    /* adding EventHandler to the button */
    submitButton.setOnAction(new EventHandler<ActionEvent>(){

      @Override
      public void handle(ActionEvent e){

        String message="";
        String sNumber = serialNumber.getText();

        if(validateValue(sNumber) == true){
          message = "The check digit for locomotive "+ sNumber +" is "+generateCheckDigit(sNumber)+".";
        }

        else{
          message = "Invalid input. Must be an 11 digits long number.";
        }

        showMessage(message);

        /* A result message has been displayed, so we reset the variable used with input length message */
        if (lenghtLimitMessageDislayed == true){
          lenghtLimitMessageDislayed = false;
        }

      }

    });

    /* creating a Clear button, with C as its mnemonic */
    Button clearButton = new Button("_Clear");

    /* setting the Clear button as cancel button, to be called if the user presses Escape key */
    clearButton.setCancelButton(true);

    /* adding EventHandler to the button */
    clearButton.setOnAction(new EventHandler<ActionEvent>(){
      @Override
      public void handle(ActionEvent e){
        serialNumber.clear();
        showMessage("");
      }
    });

    /* creating an HBox */
    HBox buttonBox = new HBox();

    /* adding children to the HBox */
    buttonBox.getChildren().addAll(submitButton, clearButton);

    /* setting the horizontal spacing between children to 5px */
    buttonBox.setSpacing(5);

    /* creating a VBox */
    VBox root = new VBox();

    /* adding the children to the VBox */
    root.getChildren().addAll(locomotiveImageView, inputMessage, serialNumber, buttonBox, result);

    /* setting the vertical spacing between children to 5px */
    root.setSpacing(5);

    /* setting the minimum Size of the VBox */
    root.setMinSize(350, 300);

    /* setting the style for the VBox */
    root.setStyle("-fx-padding: 10;"
                + "-fx-border-width: 2;"
                + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;"
                + "-fx-border-color: #1E90FF;");

    /* creating the Scene */
    Scene scene = new Scene(root);

    /* adding the scene to the Stage */
    stage.setScene(scene);

    /* setting the title of the Stage */
    stage.setTitle("Locomotive Check Digit Generator");

    /* showing the Stage */
    stage.show();

  }

  private void showMessage(String message){
    result.setText(message);
  }

}
