package com.nikitayankov.rx;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nikitayankov.rx.Retrofit.Channel;
import com.nikitayankov.rx.Retrofit.Feed;
import com.nikitayankov.rx.Retrofit.FeedItem;
import com.nikitayankov.rx.Retrofit.RssAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

// TODO: 29.07.2017 Add ability to save an article in SQLite and query it in "Saved" page
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.list_items)
    RecyclerView mRecyclerView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.navigation)
    NavigationView mNavigationView;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    ChannelAdapter mChannelAdapter;
    LinearLayoutManager mLinearLayoutManager;

    ArrayList<FeedItem> mFeedItems = new ArrayList<>();

    Callback<Feed> mDefaultCallback;
    RssAdapter mRssAdapter;

    ActionBarDrawerToggle mDrawerToggle;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mToolbar.setTitle("Retrofit 2 RSS");
        setSupportActionBar(mToolbar);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,  R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mChannelAdapter = new ChannelAdapter(mFeedItems);
        mLinearLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mChannelAdapter);

        mNavigationView.setNavigationItemSelectedListener(this);

        mDefaultCallback = new Callback<Feed>() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                Feed feed = (Feed) response.body();

                if (feed != null) {
                    Channel channel = feed.getChannel();
                    updateFeed(channel.getFeedItems());
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.goha.ru/")
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        mRssAdapter = retrofit.create(RssAdapter.class);

        createFeedCall("all").enqueue(mDefaultCallback);
    }

    private Call<Feed> createFeedCall(String category) {
        return mRssAdapter.GetItems(category);
    }

    private void updateFeed(ArrayList<FeedItem> feedItems) {
        this.mFeedItems.clear();
        this.mFeedItems.addAll(feedItems);
        this.mChannelAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Call<Feed> feedCall;

        mDrawerLayout.closeDrawer(Gravity.START);

        switch (item.getItemId()) {
            case R.id.menu_all:
                feedCall = createFeedCall("all");
                feedCall.enqueue(mDefaultCallback);
                break;

            case R.id.menu_articles:
                feedCall = createFeedCall("articles");
                feedCall.enqueue(mDefaultCallback);
                break;

            case R.id.menu_cybersport:
                feedCall = createFeedCall("cybersport");
                feedCall.enqueue(mDefaultCallback);
                break;

            case R.id.menu_hearthstone:
                feedCall = createFeedCall("hearthstone-heroes-of-warcraft");
                feedCall.enqueue(mDefaultCallback);
                break;

            case R.id.menu_dota2:
                feedCall = createFeedCall("dota2");
                feedCall.enqueue(mDefaultCallback);
                break;

            case R.id.menu_starcraft2:
                feedCall = createFeedCall("starcraft2");
                feedCall.enqueue(mDefaultCallback);
                break;

            case R.id.menu_league_of_legends:
                feedCall = createFeedCall("league-of-legends");
                feedCall.enqueue(mDefaultCallback);
                break;

            case R.id.menu_the_witcher_3:
                feedCall = createFeedCall("witcher");
                feedCall.enqueue(mDefaultCallback);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {
        ArrayList<FeedItem> mFeedItems;

        ChannelAdapter(ArrayList<FeedItem> feedItems) {
            this.mFeedItems = feedItems;
        }

        @Override
        public ChannelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.list_item, parent, false);

            return new ChannelViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ChannelViewHolder holder, int position) {
            FeedItem feedItem = mFeedItems.get(position);

            holder.mTitle.setText(prepareTitle(feedItem.getTitleString()));
            holder.mLink.setText(feedItem.getLinkString());
            holder.mDescription.setText(prepareDescription(feedItem.getDescriptionString()));
            holder.mLinkAuthor.setText(feedItem.getAuthorString());
            holder.mPublicationDate.setText(prepareDate(feedItem.getDateString()));
            holder.mCategory.setText(feedItem.getCategoryString());

            holder.mLinkString = feedItem.getLinkString();
            holder.mTitleString = prepareTitle(feedItem.getTitleString());
            holder.mCategoryString = feedItem.getCategoryString();
        }

        @Override
        public int getItemCount() {
            return this.mFeedItems.size();
        }

        String prepareTitle(String title) {
            return Html.fromHtml(title).toString();
        }

        String prepareDescription(String description) {
            return Html.fromHtml(description).toString().substring(0, 250).concat("...");
        }

        String prepareDate(String date) {
            String prepared;

            SimpleDateFormat format = new SimpleDateFormat("dd MMMM hh:mm", Locale.ENGLISH);
            prepared = format.format(Date.parse(date));

            return prepared;
        }

        class ChannelViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.item_title)
            TextView mTitle;

            @BindView(R.id.item_link)
            TextView mLink;

            @BindView(R.id.item_description)
            TextView mDescription;

            @BindView(R.id.item_link_author)
            TextView mLinkAuthor;

            @BindView(R.id.item_publication_date)
            TextView mPublicationDate;

            @BindView(R.id.item_category)
            TextView mCategory;

            String mLinkString;
            String mTitleString;
            String mCategoryString;

            // TODO: 29.07.2017 - Open a new activity with WebView. Store id / link in database and mark as read;
            @OnClick(R.id.card)
            void open() {
                Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                intent.putExtra("link", mLinkString);
                intent.putExtra("title", mTitleString);
                intent.putExtra("category", mCategoryString);

                startActivity(intent);
            }

            ChannelViewHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mDrawerToggle.syncState();
    }
}
