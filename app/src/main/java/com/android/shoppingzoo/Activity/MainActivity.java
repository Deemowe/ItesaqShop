package com.android.shoppingzoo.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.shoppingzoo.Admin.CustomersOrders;
import com.android.shoppingzoo.Model.User;
import com.android.shoppingzoo.Model.Utils;
import com.android.shoppingzoo.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

import static com.android.shoppingzoo.Fragment.HomeFragment.clearClicked;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private AppBarConfiguration mAppBarConfiguration;

    //related profile image
    int PICK_IMAGE_REQUEST = 111;
    Uri filePath;

    private ProgressBar progressBar;
    private CircleImageView UserProfileImg;
    private CircleImageView ChangeProfileBtn;
    private TextView UserNameDrawer;
    private String downloadImageUrl;
    StorageReference storageRef;
    DatabaseReference myRootRef;
    private FirebaseAuth mAuth;
    User user;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0);
        ChangeProfileBtn = hView.findViewById(R.id.nav_edit_profile_button);
        UserProfileImg = hView.findViewById(R.id.layout_profile_picture_image_preferred);
        UserNameDrawer = hView.findViewById(R.id.username_drawer);
        progressBar = hView.findViewById(R.id.profile_progress_bar);
        storageRef = FirebaseStorage.getInstance().getReference();
        myRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user=new User();
        Paper.init(getApplicationContext());

        settingUpClickListners();
        getProfileData();


       Utils.statusBarColor(MainActivity.this);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                    }
                } else {
                    Toast.makeText(MainActivity.this, "تم رفض طلب الأذن",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        //casting
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //setting on click listners
    private void settingUpClickListners() {
        //profile iamge change buttion Nav header
        ChangeProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            }
        });

    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void UploadImage() {
        if (filePath != null) {
            progressBar.setVisibility(View.VISIBLE);
            final StorageReference childRef = storageRef.child("user_profiles").child(System.currentTimeMillis()+".jpg");

//            //uploading the image
            final UploadTask uploadTask = childRef.putFile(filePath);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String message = e.toString();
                    Toast.makeText(MainActivity.this, "خطأ: " + message, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "يتم تحميل الصورة...", Toast.LENGTH_SHORT).show();
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            downloadImageUrl = childRef.getDownloadUrl().toString();
                            return childRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                downloadImageUrl = task.getResult().toString();
                                Log.d("imagUrl", downloadImageUrl);
                                SaveInfoToDatabase();
                            }
                        }
                    });
                }
            });

        } else {
            Toast.makeText(MainActivity.this, "اختر صورة", Toast.LENGTH_SHORT).show();
        }
    }

    private void SaveInfoToDatabase() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myRootRef.child("Users").child(currentUserId).child("photoUrl").setValue(downloadImageUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("TAG", "Saved to firebase");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("test", e.toString());
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(intent);
        }
        if (id == R.id.nav_cart) {
            Intent intent = new Intent(getApplicationContext(), CartActivity.class);
            startActivity(intent);
        }
        if (id == R.id.nav_orders) {
            Intent intent = new Intent(getApplicationContext(), CustomersOrders.class);
            intent.putExtra("type","user");
            startActivity(intent);
        }
        else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Paper.book().delete("active");
//            Paper.book().delete("user");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            Toast.makeText(this, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show();
            finish();
        }
        else if(id == R.id.nav_deleteAcc){
            AlertDialog.Builder dialogMsg = new AlertDialog.Builder(MainActivity.this);
            dialogMsg.setTitle("هل أنت متأكد؟");
            dialogMsg.setMessage("تنبيه: لن تستطيع التراجع عن هذا الخيار وسيتم حذف الحساب بالكامل");
            dialogMsg.setPositiveButton("حذف", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deleteUser();
                }
            });
            dialogMsg.setNegativeButton("تراجع", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alterdialog = dialogMsg.create();
            alterdialog.show();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                //getting image from gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting image to ImageView
                UserProfileImg.setImageBitmap(bitmap);
                UploadImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getProfileData() {

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user=new User();
                    user=dataSnapshot.getValue(User.class);

                    if(user!=null){
                        Log.d("usertest",user.getPhotoUrl());
                    }
                    else{
                        Log.d("usertest",user.toString());
                    }
                    String image = dataSnapshot.child("photoUrl").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
//                    Email = dataSnapshot.child("email").getValue().toString();
//                    Password = dataSnapshot.child("pass").getValue().toString();
                    try {
                        if (image != null && !image.equals("")) {
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(UserProfileImg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    UserNameDrawer.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("deleteing Tag", "User account deleted.");
                                // Show a message to the user indicating success
                                Toast.makeText(MainActivity.this, "تم حذف الحساب بنجاح", Toast.LENGTH_SHORT).show();
                                // Redirect the user to the login screen or any other screen you desire
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.d("deleteingTag", "Failed to delete user account.");
                                // Show a message to the user indicating failure
                                Toast.makeText(MainActivity.this, "تعذر حذف الحساب", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
}
}


}