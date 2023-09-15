package com.grandra.medicinal_herib;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "herb.db";
    ArrayList<Favroite_herb> favroiteherbs = new ArrayList<>();
    // DBHelper 생성자
    public DBHelper(Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    // Person Table 생성
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Person(name TEXT, Age TEXT, ADDR TEXT)");
    }

    // Person Table Upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Person");
        onCreate(db);
    }

    // Person Table 데이터 입력
    public void insert(String name, String herb_num, String imageUrl) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO Person VALUES('" + name + "', '" + herb_num + "', '" + imageUrl + "')");
        db.close();
    }

    // Person Table 데이터 수정
    public void Update(String name, int age, String Addr) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE Person SET age = " + age + ", ADDR = '" + Addr + "'" + " WHERE NAME = '" + name + "'");
        db.close();
    }

    // Person Table 데이터 삭제
    public void Delete(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM Person WHERE NAME = '" + name + "'");
        db.close();
    }

    // Person Table 조회
    public String getResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";


        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM Person", null);
        while (cursor.moveToNext()) {


            favroiteherbs.add(new Favroite_herb(cursor.getString(0),cursor.getString(1),cursor.getString(2)));
            for (Favroite_herb herbs : favroiteherbs) {
                Log.v("태그", "이름: " + herbs.getName() + ", 번호: " + herbs.getNum() + ", 이미지주소: " + herbs.getImageUrl());
            }

        }
        return result;
    }

    public ArrayList<Favroite_herb> getFavroiteplantsss(){
        return favroiteherbs;
    }

    // 해당 아이템이 즐겨찾기에 등록되어 있는지 확인하는 메서드
    public boolean isFavorite(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Person WHERE name = ?", new String[]{name});

        boolean isFavorite = cursor.getCount() > 0; // 데이터가 존재하면 즐겨찾기에 등록된 것

        cursor.close();
        return isFavorite;
    }
}
