package com.grandra.medicinal_herib;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder>{
    private boolean hideCheckBoxLayout = false;

    private ArrayList<String> imageUrl;
    private ArrayList<String> herbName;
    private ArrayList<String> herbNum;
    private Context context; // Context 변수 추가

    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_AD = 1;

    public CustomAdapter(Context context, ArrayList<String> imageUrl, ArrayList<String> herbName, ArrayList<String> herbNum) {
        this.imageUrl = imageUrl;
        this.herbName = herbName;
        this.herbNum = herbNum;
        this.context = context;
    }

    public void setHideCheckBoxLayout(boolean hide) {
        hideCheckBoxLayout = hide;
        notifyDataSetChanged();
    }

    //===== [Click 이벤트 구현을 위해 추가된 코드] ==========================
    // OnItemClickListener 인터페이스 선언
    public interface OnItemClickListener {
        void onItemClicked(int position, String data,String image_data);

        void onItemClicked(int position, String data);
    }

    // OnItemClickListener 참조 변수 선언
    private OnItemClickListener itemClickListener;

    // OnItemClickListener 전달 메소드
    public void setOnItemClickListener (OnItemClickListener listener) {
        itemClickListener = listener;
    }
    //======================================================================



    //===== 뷰홀더 클래스 =====================================================
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private ImageView imageView;
        private CheckBox checkBox;
        LinearLayout checkBoxLayout; // 추가
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
            checkBox = itemView.findViewById(R.id.Favorite_Check);
            checkBoxLayout = itemView.findViewById(R.id.checkBoxLayout);
        }

        public ImageView getImageView(){return imageView;}
        public TextView getTextView() {
            return textView;
        }
        public CheckBox getCheckBox(){return checkBox;}
    }
    //========================================================================

    @NonNull
    @Override   // ViewHolder 객체를 생성하여 리턴한다.
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    int position = viewHolder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        String data = herbNum.get(position);
                        String image_data = imageUrl.get(position);
                        itemClickListener.onItemClicked(position, data, image_data);
                    }
                }
            }
        });

        return viewHolder;

    }

    @Override   // ViewHolder안의 내용을 position에 해당되는 데이터로 교체한다.
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // RequestOptions를 사용하여 둥글게 처리 설정
        RequestOptions requestOptions = new RequestOptions()
                .transform(new RoundedCorners(20)) // 둥글게 처리를 위한 RoundedCorners 변환 적용
                .diskCacheStrategy(DiskCacheStrategy.ALL); // 디스크 캐싱 전략 설정


        String name = herbName.get(position);
        holder.textView.setText(name);
        Glide.with(context)
                .load(imageUrl.get(position))
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade()) // 이미지 로딩 시 CrossFade 효과 적용
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 디스크 캐싱 전략 설정
                .into(holder.imageView);

        if (hideCheckBoxLayout) {
            holder.checkBoxLayout.setVisibility(View.GONE);
        } else {
            holder.checkBoxLayout.setVisibility(View.VISIBLE);
        }


        String Title = herbName.get(position);
        DBHelper dbHelper1 = new DBHelper(context, 1);
        boolean isFavorite = dbHelper1.isFavorite(Title);

        holder.getCheckBox().setChecked(isFavorite); // 아이템의 즐겨찾기 상태에 따라 체크박스 상태 설정

        holder.getCheckBox().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                boolean isChecked = checkBox.isChecked();

                String title = herbName.get(position);
                DBHelper dbHelper = new DBHelper(context, 1);

                if (isChecked) {
                    Toast.makeText(context, "즐겨찾기 등록", Toast.LENGTH_SHORT).show();
                    dbHelper.insert(title, herbNum.get(position), imageUrl.get(position));
                } else {
                    Toast.makeText(context, "즐겨찾기 해제", Toast.LENGTH_SHORT).show();
                    dbHelper.Delete(title);
                }
            }
        });
    }

    @Override   // 전체 데이터의 갯수를 리턴한다.
    public int getItemCount() {
        return herbName.size();
    }


}