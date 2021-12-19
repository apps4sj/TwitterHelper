package com.apps4sj.TwitterSeller;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.R.drawable;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import java.io.OutputStream;

import org.json.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;

//https://code.tutsplus.com/tutorials/android-from-scratch-how-to-store-application-data-locally--cms-26853
public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final String PREVIEW_IMAGE_BYTES = "com.example.camerainput.PREVIEWBYTEARRAY";
    public static final String LISTING_ID = "com.example.camerainput.LISTINGID";
    public static final String SAVE_INSTANCE = "com.example.camerainput.SAVE_INSTANCE";
    public static final String EDITING = "com.example.camerainput.EDITING";

    public static SQLConnection sql = null;

    public static final String HOST = "apps4sj.org";
    public static final int PORT = 32421;
    private Socket socket;

    private String[] currentPhotoPaths = {"", "", ""};
    private int currentPhotoNum = 0;

    //private Button pictureButton;
    private Button sendButton, myListingsButton;
    private EditText productInput, descInput, priceInput, emailInput, phoneNumInput, locationInput;
    private ImageView[] imageViewPreviews;
    private final int REQUEST_CODE_READ_PHONE_NUMBER = 0;

    private boolean editing;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setWaitingAnimation(View.GONE);

        if (sql == null) {
            sql = new SQLConnection(this);
//            sql.createTable();
            sql.printAllListings();
//            sql.dropTable();
        }

        //pictureButton = findViewById(R.id.buttonPicture);
        sendButton = findViewById(R.id.sendButton);
        myListingsButton = findViewById(R.id.buttonMyListings);
        productInput = findViewById(R.id.productEditText);
        descInput = findViewById(R.id.editTextDescription);
        priceInput = findViewById(R.id.editTextPrice);
        emailInput = findViewById(R.id.editTextEmail);
        phoneNumInput = findViewById(R.id.editTextPhoneNum);
        locationInput = findViewById(R.id.locationEditText);
        phoneNumInput = findViewById(R.id.editTextPhoneNum);
        imageViewPreviews = new ImageView[]{findViewById(R.id.imageViewPreview1), findViewById(R.id.imageViewPreview2), findViewById(R.id.imageViewPreview3)};

        Intent intent = getIntent();

        String jsonSave = intent.getStringExtra(MainActivity.SAVE_INSTANCE);
        if (jsonSave == null && ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_NUMBERS) !=
                PackageManager.PERMISSION_GRANTED) {
            // You can directly ask for the permission.
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_NUMBERS}, REQUEST_CODE_READ_PHONE_NUMBER);
        }

        if (jsonSave != null && !jsonSave.equals("")) {
            setSaveInstance(jsonSave);
            System.out.println("setting save");
        } else {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_PHONE_NUMBERS) ==
                    PackageManager.PERMISSION_GRANTED) {
                TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                String phoneNumber = tMgr.getLine1Number();
                phoneNumInput.setText(phoneNumber);
            }
        }

        editing = intent.getBooleanExtra(MainActivity.EDITING, false);

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.take_a_picture);

        for (ImageView imageView : imageViewPreviews) {
            imageView.setImageDrawable(drawable);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v == imageViewPreviews[0]) {
                        currentPhotoNum = 0;
                    }
                    if (v == imageViewPreviews[1]) {
                        currentPhotoNum = 1;
                    }
                    if (v == imageViewPreviews[2]) {
                        currentPhotoNum = 2;
                    }

                    if (currentPhotoNum == 3) {
                        Toast.makeText(MainActivity.this, "You have reached the maximum number of pictures", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        // Ensure that there's a camera activity to handle the intent
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                                        "com.apps4sj.TwitterSeller",
                                        photoFile);
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
                        }
                    }
                }
            });
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                Thread thread = new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {
                        try {
                            setWaitingAnimation(View.VISIBLE);
                            Looper.prepare();
                            socket = new Socket(HOST, PORT);
                            OutputStream outputStream = socket.getOutputStream();
                            InputStream inputStream = socket.getInputStream();

                            byte[] binaryImage1 = {};
                            byte[] binaryImage2 = {};
                            byte[] binaryImage3 = {};
                            if (!currentPhotoPaths[0].equals("")) {
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                Bitmap imageBitmap = rotateBitmap(currentPhotoPaths[0]);
                                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                                binaryImage1 = stream.toByteArray();
                            }
                            if (!currentPhotoPaths[1].equals("")) {
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                Bitmap imageBitmap = rotateBitmap(currentPhotoPaths[1]);
                                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                                binaryImage2 = stream.toByteArray();
                            }
                            if (!currentPhotoPaths[2].equals("")) {
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                Bitmap imageBitmap = rotateBitmap(currentPhotoPaths[2]);
                                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                                binaryImage3 = stream.toByteArray();
                            }

                            JSONObject toSend = new JSONObject();

                            if (editing) toSend.put("type", "edit");
                            else toSend.put("type", "stage");
                            String id = String.valueOf((int) (Math.random() * 999999999));
                            toSend.put("id", id);
                            toSend.put("itemName", productInput.getText());
                            toSend.put("price", String.valueOf(priceInput.getText()));
                            toSend.put("description", descInput.getText());
                            String location = locationInput.getText().toString();
                            if (location.equals("")) {
                                location = findLocation(MainActivity.this);
                            }
                            toSend.put("location", location);

                            JSONObject contact = new JSONObject();
                            contact.put("email", emailInput.getText());
                            contact.put("phoneNum", phoneNumInput.getText());
                            toSend.put("contact", contact);

                            if (binaryImage1.length > 0) {
                                JSONObject image0 = new JSONObject();
                                image0.put("fileName", "image0.jpg");
                                image0.put("length", binaryImage1.length);
                                toSend.put("image0", image0);
                            }
                            if (binaryImage2.length > 0) {
                                JSONObject image1 = new JSONObject();
                                image1.put("fileName", "image1.jpg");
                                image1.put("length", binaryImage2.length);
                                toSend.put("image1", image1);
                            }
                            if (binaryImage3.length > 0) {
                                JSONObject image2 = new JSONObject();
                                image2.put("fileName", "image2.jpg");
                                image2.put("length", binaryImage3.length);
                                toSend.put("image2", image2);
                            }

                            int headerNum = toSend.toString().getBytes().length + binaryImage1.length + binaryImage2.length + binaryImage3.length;
                            StringBuilder header = new StringBuilder(String.valueOf(headerNum));
                            while (header.length() < 9) {
                                header.insert(0, "0");
                            }

                            String endJson = "\n";

                            outputStream.write(header.toString().getBytes());
                            outputStream.write(toSend.toString().getBytes());
                            outputStream.write(endJson.getBytes());
                            if (binaryImage1.length > 0) {
                                outputStream.write(binaryImage1);
                            }
                            if (binaryImage2.length > 0) {
                                outputStream.write(binaryImage2);
                            }
                            if (binaryImage3.length > 0) {
                                outputStream.write(binaryImage3);
                            }

                            System.out.println(toSend.toString());

                            sql.addListing(new Listing(id, productInput.getText().toString(),
                                    descInput.getText().toString(), emailInput.getText().toString(),
                                    locationInput.getText().toString(), phoneNumInput.getText().toString(),
                                    priceInput.getText().toString(), currentPhotoPaths[0],
                                    currentPhotoPaths[1], currentPhotoPaths[2],
                                    new SimpleDateFormat("MM-dd-yyyy").format(new Date())));

                            sql.printAllListings();


                            byte[] header_buffer = new byte[10];
                            int charsIn = inputStream.read(header_buffer);
                            String data = new String(header_buffer, StandardCharsets.UTF_8);
                            int filesize = Integer.parseInt(data);
                            System.out.println("File Size: " + filesize);

                            byte[] image = new byte[filesize];
                            int bytesRead = 0;
                            int currentTotal = 0;
                            do {
                                bytesRead = inputStream.read(image, currentTotal, (image.length - currentTotal));
                                if (bytesRead > 0)
                                    currentTotal += bytesRead;
                            } while (bytesRead > 0);

                            socket.close();

                            setWaitingAnimation(View.GONE);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                                    intent.putExtra(PREVIEW_IMAGE_BYTES, image);
                                    intent.putExtra(LISTING_ID, id);
                                    intent.putExtra(SAVE_INSTANCE, getSaveInstance());
                                    startActivity(intent);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
        });

        myListingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeleteActivity.class);
                startActivity(intent);
            }
        });
    }

    private String getSaveInstance() {
        try {
            JSONObject saveInstance = new JSONObject();
            saveInstance.put("itemName", productInput.getText());
            saveInstance.put("price", String.valueOf(priceInput.getText()));
            saveInstance.put("description", descInput.getText());
            saveInstance.put("location", locationInput.getText());

            JSONObject contact = new JSONObject();
            contact.put("email", emailInput.getText());
            contact.put("phoneNum", phoneNumInput.getText());
            saveInstance.put("contact", contact);

            if (!currentPhotoPaths[0].equals("")) {
                saveInstance.put("image0", currentPhotoPaths[0]);
            }
            if (!currentPhotoPaths[1].equals("")) {
                saveInstance.put("image1", currentPhotoPaths[1]);
            }
            if (!currentPhotoPaths[2].equals("")) {
                saveInstance.put("image2", currentPhotoPaths[2]);
            }
            return saveInstance.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private void setSaveInstance(String savedInstance) {
        try {
            System.out.println(savedInstance);
            JSONObject json = new JSONObject(savedInstance);
            productInput.setText(json.get("itemName").toString());
            priceInput.setText(json.get("price").toString());
            descInput.setText(json.get("description").toString());
            locationInput.setText(json.get("location").toString());

            JSONObject contact = (JSONObject) json.get("contact");
            emailInput.setText(contact.get("email").toString());
            phoneNumInput.setText(contact.get("phoneNum").toString());

            if (!json.isNull("image0")) {
                Bitmap imageBitmap = rotateBitmap(json.get("image0").toString());
                imageViewPreviews[0].setImageBitmap(imageBitmap);
                currentPhotoPaths[0] = json.get("image0").toString();
                currentPhotoNum = 1;
            }
            if (!json.isNull("image1")) {
                Bitmap imageBitmap = rotateBitmap(json.get("image1").toString());
                imageViewPreviews[1].setImageBitmap(imageBitmap);
                currentPhotoPaths[1] = json.get("image1").toString();
                currentPhotoNum = 2;
            }
            if (!json.isNull("image2")) {
                Bitmap imageBitmap = rotateBitmap(json.get("image2").toString());
                imageViewPreviews[2].setImageBitmap(imageBitmap);
                currentPhotoPaths[2] = json.get("image2").toString();
                currentPhotoNum = 3;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && !currentPhotoPaths[currentPhotoNum - 1].equals("")) {
            try {
                Bitmap imageBitmap = rotateBitmap(currentPhotoPaths[currentPhotoNum - 1]);
                imageViewPreviews[currentPhotoNum - 1].setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap rotateBitmap(String photoPath) throws IOException {
        ExifInterface ei = new ExifInterface(photoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                return bitmap;
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPaths[currentPhotoNum] = image.getAbsolutePath();
        currentPhotoNum++;
        return image;
    }


    public String findLocation(Context con) {
        LocationManager locationManager = (LocationManager) con.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return "No location";
            }
            locationManager.requestLocationUpdates(provider, 1000, 0,
                    new LocationListener() {

                        public void onLocationChanged(Location location) {
                        }

                        public void onProviderDisabled(String provider) {
                        }

                        public void onProviderEnabled(String provider) {
                        }

                        public void onStatusChanged(String provider, int status,
                                                    Bundle extras) {
                        }
                    });
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                return convertLocationToAddress(location); //new String[]{String.valueOf(latitude), String.valueOf(longitude)};
            }
        }
        return "No location";
    }

    // https://stackoverflow.com/questions/12102570/how-to-convert-gps-coordinates-to-locality
    private String convertLocationToAddress(Location location) {
        String addressText;
        String errorMessage = "";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );
        } catch (IOException ioException) {
            // Network or other I/O issues
            errorMessage = "NO INTERNET";
            System.out.println(errorMessage);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Invalid long / lat
            errorMessage = "INVALID COORDS";
            System.out.println(errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude());
        }

        // No address was found
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "NO ADDRESS FOUND";
            }
            addressText = String.valueOf(location.getLatitude()) + location.getLongitude();

        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines, join them, and return to thread
            // Only includes city and state
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            System.out.println(addressFragments.toString());
            addressFragments = new ArrayList<String>(Arrays.asList(addressFragments.get(0).split(", ")));
            addressFragments.remove(0);
            System.out.println(addressFragments.toString());

            addressText =
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments);
        }

        return addressText;

    }

    private void setWaitingAnimation(final int visibility) {
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loadingPanel).setVisibility(visibility);
            }
        });
    }
}
