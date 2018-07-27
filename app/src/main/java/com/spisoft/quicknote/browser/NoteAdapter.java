package com.spisoft.quicknote.browser;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.os.Handler;

/**
 * Created by alexandre on 03/02/16.
 */
public class NoteAdapter extends RecyclerView.Adapter implements NoteInfoRetriever.NoteInfoListener {

    private static final int NOTE = 0;
    protected final Context mContext;
    private final HashMap<Note, String> mText;
    private final float mBigText;
    private final float mMediumText;
    private final float mSmallText;
    private final NoteInfoRetriever mNoteInfoRetriever;
    private final NoteThumbnailEngine mNoteThumbnailEngine;

    private final Handler mHandler;
    private final String mLoadingText;
    protected List<Object> mNotes;
    private ArrayList<Object> mSelelectedNotes;

    public NoteAdapter(Context context, List<Object> notes) {
        super();
        mContext = context;
        mHandler = new Handler();
        mNotes = notes;
        mText = new HashMap<Note, String>();
        mBigText = mContext.getResources().getDimension(R.dimen.big_text);
        mMediumText = mContext.getResources().getDimension(R.dimen.medium_text);
        mSmallText = mContext.getResources().getDimension(R.dimen.small_text);
        mNoteInfoRetriever = new NoteInfoRetriever(this, context);
        mNoteThumbnailEngine = new NoteThumbnailEngine(context);
        mLoadingText = context.getResources().getString(R.string.loading);

    }

    public void setNotes(List<Object> notes) {
        int i = 0;
        List<Object> oldNotes = new ArrayList<>(mNotes);
        List<Object> oldNotes1 = new ArrayList<>(mNotes);
        List<Object> newNotes = new ArrayList<>(notes);
        List<Object> toRemove = new ArrayList<>();
        mNotes = notes;
        for (Object note : oldNotes1) {
            if (!notes.contains(note)) {
                i = oldNotes.indexOf(note);
                oldNotes.remove(i);
                notifyItemRemoved(i);
            }
        }
        for (Object note : notes) {
            if (!oldNotes.contains(note)) {
                notifyItemInserted(oldNotes.size());
                oldNotes.add(note);
            }
        }
        i = 0;
        oldNotes1 = new ArrayList<>(oldNotes);
        for (Object note : oldNotes) {
            int newPos = notes.indexOf(note);
            int currentPos = oldNotes1.indexOf(note);
            if (newPos >= 0) {
                if(newPos != currentPos) {
                    newNotes.remove(note);
                    oldNotes1.remove(currentPos);
                    oldNotes1.add(newPos, note);
                    notifyItemMoved(currentPos, newPos);
                }
                if(note instanceof Note && ((Note)note).isPinned != ((Note)notes.get(i)).isPinned){
                    notifyItemChanged(newPos);
                }

            }
            if (newPos == -1)
                toRemove.add(note);

            i++;
        }


    }

    public void addNote(Object note) {
        mNotes.add(note);
        notifyItemInserted(mNotes.size() - 1);
    }

    public void setText(Note note, String text) {
        mText.put(note, text);
        int index = mNotes.indexOf(note);
        mNotes.remove(index);
        mNotes.add(index, note);
        notifyItemChanged(index);

    }

    public void onItemMove(int adapterPosition, int adapterPosition1) {
        Note note = (Note) mNotes.get(adapterPosition);
        mNotes.remove(adapterPosition);
        mNotes.add(adapterPosition1, note);
        notifyItemMoved(adapterPosition, adapterPosition1);
    }

    public void toggleNote(Object note, View v) {
        if (mSelelectedNotes == null)
            mSelelectedNotes = new ArrayList<>();
        if (mSelelectedNotes.contains(note)) {
            Log.d("selectdebug", "remove");
            mSelelectedNotes.remove(note);
        } else {
            Log.d("selectdebug", "add");
            mSelelectedNotes.add(note);
        }
        notifyItemChanged(mNotes.indexOf(note));
    }

    public List<Object> getSelectedObjects() {
        return mSelelectedNotes;
    }

    public void clearSelection() {
        if (mSelelectedNotes != null)
            mSelelectedNotes.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onNoteInfo(Note note) {
        setText(note, note.shortText);
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {


        private final View mCard;
        private final TextView mTitleView;
        private final TextView mTextView;
        private final TextView mMarkView;
        private final ImageView mPreview1;
        private final ImageView mPreview2;
        private Note mNote;

        public NoteViewHolder(View itemView) {
            super(itemView);
            mCard = itemView.findViewById(R.id.cardview);
            mTitleView = (TextView) itemView.findViewById(R.id.name_tv);
            mTextView = (TextView) itemView.findViewById(R.id.text_tv);
            mMarkView = (TextView) itemView.findViewById(R.id.mark_tv);
            mPreview1= (ImageView) itemView.findViewById(R.id.preview1);
            mPreview2 = (ImageView) itemView.findViewById(R.id.preview2);
        }

        public void setName(String title) {
            if(title.startsWith("untitled"))
                mTitleView.setVisibility(View.GONE);
            else {
                mTitleView.setVisibility(View.VISIBLE);
                mTitleView.setText(title);
            }
        }

        public void setNote(final Note note) {
            if(!note.equals(getNote())){
                //reset preview
                setPreview1(null);
                setPreview2(null);
            }
            mNote = note;
            mCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSelelectedNotes != null && mSelelectedNotes.size() > 0)
                        mOnNoteClick.onLongClick(note, view);
                    else
                        mOnNoteClick.onNoteClick(note, view);
                }
            });
            mCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mOnNoteClick.onLongClick(note, view);
                    return true;
                }
            });
            itemView.findViewById(R.id.optionsButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnNoteClick.onInfoClick(note, view);
                }
            });
            setName(note.title);
            setText(note.shortText.isEmpty() && note.needsUpdateInfo ? mLoadingText:note.shortText);
            setRating(note.mMetadata.rating);
            setDate(note.mMetadata.last_modification_date);
            setKeywords(note.mMetadata.keywords);
        }

        public void setPreview1(Bitmap bitmap) {
            if(bitmap == null)
                mPreview1.setVisibility(View.GONE);
            else
                mPreview1.setVisibility(View.VISIBLE);
            mPreview1.setImageBitmap(bitmap);
        }

        public void setPreview2(Bitmap bitmap) {
            if(bitmap == null)
                mPreview2.setVisibility(View.GONE);
            else
                mPreview2.setVisibility(View.VISIBLE);
            mPreview2.setImageBitmap(bitmap);
        }

        public void setText(String s) {
            if(s==null)
                s = "";
            mTextView.setText(s);
            if(s.length()<40){
                mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,mBigText);
            } else if(s.length()<100){
                mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,mMediumText);
            }else
                mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,mSmallText);


        }
        public void setRating(int rating){
            String ratingStr = "";
            if(rating>=0) ratingStr = " "+rating + "★";
            mMarkView.setText(ratingStr);
        }
        public void setDate(long lastModified) {
            String date = "";

            if (lastModified != -1) {
                date = new SimpleDateFormat("dd/MM/yyyy").format(new Date(lastModified));
            }
            ((TextView) itemView.findViewById(R.id.date_tv)).setText(date);

        }

        public void setKeywords(List<String> keywords) {
            ViewGroup keywordsView = (ViewGroup) itemView.findViewById(R.id.keywords);
            keywordsView.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            for (String keyword : keywords) {
                View keyview = inflater.inflate(R.layout.keyword_item, null);
                ((TextView) keyview.findViewById(R.id.textView)).setText(keyword);
                keywordsView.addView(keyview);

            }
        }

        public void setSelected(boolean contains) {
            mCard.setSelected(contains);

        }

        public Note getNote() {
            return mNote;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("notedebug", "" + viewType);
        if (viewType == NOTE)
            return new NoteViewHolder(LayoutInflater.from(mContext).inflate(R.layout.grid_note_layout, null));
        else return null;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == NOTE) {
            final Note note = (Note) mNotes.get(position);
            final NoteViewHolder viewHolder = (NoteViewHolder) holder;
            if(viewHolder.getNote()!=null){
                //first we detach it
                mNoteInfoRetriever.cancelNote(viewHolder.getNote());
                mNoteThumbnailEngine.cancelNote(viewHolder.getNote());
            }
            viewHolder.setNote(note);

            viewHolder.setSelected(mSelelectedNotes != null && mSelelectedNotes.contains(note));
            Log.d(" ", note.title);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(note.equals(viewHolder.getNote()) && note.needsUpdateInfo)
                        mNoteInfoRetriever.addNote(note);
                }
            },500);

            if(note.previews.size()>0){
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(note.equals(viewHolder.getNote()))
                            mNoteThumbnailEngine.addNote(note, viewHolder);
                    }
                },1000);
            }

        }

    }

    public int getItemViewType(int position) {
        if (mNotes.get(position) instanceof Note)
            return NOTE;
        return -1;
    }

    OnNoteItemClickListener mOnNoteClick;

    void setOnNoteClickListener(OnNoteItemClickListener listener) {
        mOnNoteClick = listener;
    }

    public interface OnNoteItemClickListener {
        public void onNoteClick(Note note, View v);

        void onInfoClick(Note note, View v);

        void onLongClick(Note note, View v);
    }

    @Override
    public int getItemCount() {
        Log.d("notedebug", "mNotes " + mNotes.size());

        return mNotes.size();
    }
}
