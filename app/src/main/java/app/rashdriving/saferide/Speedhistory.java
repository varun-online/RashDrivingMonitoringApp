package app.rashdriving.saferide;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class Speedhistory extends AppCompatActivity {

    private LinearLayout locationContainer;
    private TextView noDataTextView;
    private DatabaseReference historyRef;
    private ImageButton btnDownloadPDF;
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speedhistory);

        historyRef = FirebaseDatabase.getInstance().getReference("Speedhistory");

        locationContainer = findViewById(R.id.locationContainer);
        noDataTextView = findViewById(R.id.no_data_textview);
        ImageButton redirectdash = findViewById(R.id.dash_spdhistry_imageButton);
        btnDownloadPDF = findViewById(R.id.file_download);

        btnDownloadPDF.setOnClickListener(v -> checkStoragePermission());

        redirectdash.setOnClickListener(v ->
                startActivity(new Intent(Speedhistory.this, Profile_activity.class)));

        fetchSpeedHistoryData();
    }

    private void fetchSpeedHistoryData() {
        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locationContainer.removeAllViews();
                boolean hasData = false;

                for (DataSnapshot data : snapshot.getChildren()) {
                    String vehicleNumber = data.child("VehicleNumber").getValue(String.class);
                    String placeName = data.child("Place").getValue(String.class);
                    Float speed = data.child("Speed").getValue(Float.class);
                    String time = data.child("Time").getValue(String.class);

                    if (vehicleNumber != null && placeName != null && speed != null && time != null) {
                        displayLocationData(vehicleNumber, placeName, speed, time);
                        hasData = true;
                    }
                }

                noDataTextView.setVisibility(hasData ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Speedhistory.this,
                        "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayLocationData(String vehicleNumber, String placeName, float speed, String time) {
        TextView locationView = new TextView(this);
        locationView.setText(
                "🚗 Vehicle No: " + vehicleNumber + "\n" +
                        "📍 Place: " + placeName + "\n" +
                        "🚗 Speed: " + String.format(Locale.getDefault(), "%.2f km/h", speed) + "\n" +
                        "🕒 Time: " + time
        );

        locationView.setPadding(16, 16, 16, 16);
        locationView.setTextSize(16f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);

        locationView.setLayoutParams(params);
        locationView.setBackgroundResource(R.drawable.item_background);

        locationContainer.addView(locationView);
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else {
                generatePDF();
            }
        } else {
            generatePDF(); // No permission needed for scoped storage
        }
    }

    private void generatePDF() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint linePaint = new Paint();

        int pageWidth = 1240, pageHeight = 1754;
        int margin = 80, y = 120, lineSpacing = 60;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        titlePaint.setTextSize(45f);
        titlePaint.setFakeBoldText(true);
        titlePaint.setUnderlineText(true);
        canvas.drawText("🚀 Speed History Report", margin + 350, y, titlePaint);
        y += 80;

        linePaint.setStrokeWidth(3);
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint);
        y += 50;

        paint.setTextSize(30f);
        paint.setFakeBoldText(true);

        for (int i = 0; i < locationContainer.getChildCount(); i++) {
            TextView textView = (TextView) locationContainer.getChildAt(i);
            String[] lines = textView.getText().toString().split("\n");

            for (String line : lines) {
                canvas.drawText(line, margin, y, paint);
                y += lineSpacing;
            }

            canvas.drawLine(margin, y, pageWidth - margin, y, linePaint);
            y += 40;

            if (y > pageHeight - 100) break;
        }

        pdfDocument.finishPage(page);

        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "SpeedHistory.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF saved in app storage", Toast.LENGTH_LONG).show();

            Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open PDF with"));

        } catch (IOException e) {
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        pdfDocument.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
                generatePDF();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
