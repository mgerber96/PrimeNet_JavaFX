package PrimeNet;

import PrimeNet.movies.Movie;
import PrimeNet.movies.Results;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;
import javafx.scene.image.ImageView;
import java.io.*;
import java.rmi.MarshalledObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Controller{
    @FXML
    public ProgressIndicator progressbar;
    @FXML
    ComboBox<String> categoriesComboBox;
    @FXML
    TextField searchField = new TextField();
    @FXML
    TableView<Film> filmTable = new TableView<>();
    @FXML
    private TableColumn<Film, String> rateColumn = new TableColumn<>("Bewerten");
    @FXML
    private TableColumn<Film, Boolean> favouriteColumn = new TableColumn<>("Favorit");
    @FXML
    private TableColumn<Film, String> titleColumn = new TableColumn<>("Filmtitel");
    @FXML
    private TableColumn<Film, Integer> yearColumn = new TableColumn<>("Jahr");
    @FXML
    private TableColumn<Film, Boolean> rememberColumn = new TableColumn<>("Merken");
    @FXML
    private TableColumn<Film, String> categoriesColumn = new TableColumn<>("Kategorien");
    @FXML
    ComboBox<String> yearComboBox = new ComboBox<>();
    @FXML
    ImageView previewPane = new ImageView();
    @FXML
    Label previewTitle = new Label();
    @FXML
    Label previewDate = new Label();
    @FXML
    private Label previewRate = new Label();
    @FXML
    Label previewOverview = new Label();

    private ObservableList<String> rate = FXCollections.observableArrayList();
    private ObservableList<Film> originalFilms = FXCollections.observableArrayList();
    private ObservableList<Film> originalFilmsForSecondFilterAction = FXCollections.observableArrayList();
    private static String titleForSearch;
    private static int yearForSearch;
    private static SimpleBooleanProperty windowCloseAction = new SimpleBooleanProperty(false);
    private static SimpleBooleanProperty doubleClickInFavouriteOrBookmarksWindow = new SimpleBooleanProperty(false);
    private static Stage favouriteWindow = new Stage();
    private static Stage bookmarksWindow = new Stage();
    private static String filmsInFavouriteAsString;
    private static String filmsInBookmarksAsString;

    public static void setWindowCloseAction(){
        if(windowCloseAction.getValue())
            windowCloseAction.set(false);
        else
            windowCloseAction.set(true);
    }

    public static void setDoubleClickInFavouriteOrBookmarksWindow(String title, int year){
        titleForSearch = title;
        yearForSearch = year;
        if(doubleClickInFavouriteOrBookmarksWindow.getValue())
            doubleClickInFavouriteOrBookmarksWindow.set(false);
        else
            doubleClickInFavouriteOrBookmarksWindow.set(true);
    }

    public static Stage getBookmarksWindow() {
        return bookmarksWindow;
    }

    public static Stage getFavouriteWindow() {
        return favouriteWindow;
    }

    @FXML
    private void initialize(){
        bookmarksWindow.initModality(Modality.APPLICATION_MODAL);
        favouriteWindow.initModality(Modality.APPLICATION_MODAL);
        windowCloseAction.addListener((observableValue, oldValue, newValue) -> {
            try{
                refreshFilmList();
            } catch (Exception e){e.printStackTrace();}
        });
        doubleClickInFavouriteOrBookmarksWindow.addListener(((observable, oldValue, newValue) ->{
            searchForThis(titleForSearch, yearForSearch);
            filmTable.getSelectionModel().select(1);
        }));
        setUpTables();
        progressbar.setProgress(-1.0f);
        progressbar.setVisible(false);
/*
        AutoCompletionBinding<Film> autoCompletionBinding =
                TextFields.bindAutoCompletion(searchField,
                        suggestionrequest -> MovieDatabase.getMoviesByName(suggestionrequest.getUserText()));
        autoCompletionBinding.setOnAutoCompleted(event -> {
            originalFilms.add(event.getCompletion());
            searchField.clear();
        });
*/
    }
    private void refreshFilmList(){
        makeFavouriteFileToString();
        makeBookmarksFileToString();
        for(Film film : originalFilmsForSecondFilterAction){
            if(isThisFilmInFavourite(film))
                film.setFavourite(true);
            else
                film.setFavourite(false);

            if(isThisFilmInBookmarks(film))
                film.setRemember(true);
            else
                film.setRemember(false);
        }
    }

    private boolean isThisFilmInFavourite(Film film){
       return HelperMethods.isThisFilmInFile(film, filmsInFavouriteAsString);
    }

    private boolean isThisFilmInBookmarks(Film film){
        return HelperMethods.isThisFilmInFile(film, filmsInBookmarksAsString);
    }

    private void searchForThis (String title, int year){

        new Thread(() -> {
            progressbar.setVisible(true);
            try{
                makeFavouriteFileToString();
                makeBookmarksFileToString();
            } catch (Exception e){e.printStackTrace();}
            try{
                originalFilms = getFilm(correctStringForSearch(title));
            } catch (Exception e ){e.printStackTrace();}

            progressbar.setVisible(false);
            filmTable.setItems(originalFilms);

            filterTableViewAccToYears(String.valueOf(year));

            addListenerToCheckBoxInFavouriteColumn();
            addListenerToCheckBoxInBookmarksColumn();
        }).start();
    }

    private void setUpTables(){
        fillCategoriesComboBox();
        fillYearComboBox();
        setUpTableOfFilm();
    }

    private void fillCategoriesComboBox(){
        categoriesComboBox.getItems().add("Alle");
        categoriesComboBox.getItems().addAll( "Abenteuer", "Action", "Animation", "Dokumentarfilm",
                "Drama", "Familie", "Fantasy", "Historie", "Horror", "Komödie", "Kriegsfilm", "Krimi",
                "Liebesfilm", "Musik", "Mystery", "Science", "Fiction", "TV-Film", "Thriller", "Western");
    }

    private void fillYearComboBox(){
        yearComboBox.getItems().add("Alle");
        for (int n = 2018; n >= 1950; n--){
            yearComboBox.getItems().add(String.valueOf(n));
        }
    }

    private void setUpTableOfFilm() {
        filmTable.setEditable(true);

        //titleColumn
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setPrefWidth(400);

        //yearColumn
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearColumn.setMaxWidth(70);
        yearColumn.setMinWidth(70);
        yearColumn.setPrefWidth(70);
        yearColumn.setStyle("-fx-alignment: CENTER;");

        //rememberColumn
        rememberColumn.setEditable(true);
        rememberColumn.setCellFactory(column -> new CheckBoxTableCell<>());
        rememberColumn.setCellValueFactory(new PropertyValueFactory<>("remember"));
        rememberColumn.setCellFactory(CheckBoxTableCell.forTableColumn(rememberColumn));
        rememberColumn.setMaxWidth(70);
        rememberColumn.setMinWidth(70);

        //favouriteColumn
        favouriteColumn.setEditable(true);
        favouriteColumn.setMaxWidth(60);
        favouriteColumn.setMinWidth(60);
        favouriteColumn.setCellValueFactory(new PropertyValueFactory<>("favourite"));
        favouriteColumn.setCellFactory(CheckBoxTableCell.forTableColumn(favouriteColumn));

        //rateColumn
        rate.addAll("Like", "Dislike");
        rateColumn.setCellValueFactory(new PropertyValueFactory<>("rate"));
        rateColumn.setCellFactory(ComboBoxTableCell.forTableColumn(new DefaultStringConverter(),rate));
        rateColumn.setMaxWidth(90);
        rateColumn.setMinWidth(90);
        rateColumn.setStyle("-fx-alignment: CENTER;");

        //categoriesColumn
        categoriesColumn.setCellValueFactory(new PropertyValueFactory<>("categories"));
        categoriesColumn.setMaxWidth(300);
        categoriesColumn.setMinWidth(300);

        filmTable.getColumns().addAll(favouriteColumn,titleColumn,categoriesColumn, yearColumn, rateColumn, rememberColumn);
    }

    public void filmTableIsClicked(){
        Film film = filmTable.getSelectionModel().getSelectedItem();
        if (film == null) {
            previewPane.setImage(null);
            previewTitle.setText("");
            previewDate.setText("");
            previewRate.setText("");
            previewOverview.setText("");
            return;
        }
        previewPane.setImage(film.getPoster());
        previewTitle.setText(film.getTitle());
        String year = Integer.toString(film.getYear());
        previewDate.setText("(" + year + ")");
        previewRate.setText(film.getRate());
        previewOverview.setText(film.getOverview());
    }

    //by clicking the button "Favoriten" favourite window will be opened
    public void clickFavourite() throws IOException{
        Parent root =  FXMLLoader.load(getClass().getResource("FxmlFiles/Favourite.fxml"));
        HelperMethods.openNewWindow(favouriteWindow, "Favoriten", root);
    }

    //by clicking the button "Merkliste" bookmarks window will be opened
    public void clickBookmarks() throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("FxmlFiles/Bookmarks.fxml"));
        HelperMethods.openNewWindow(bookmarksWindow, "Merkliste", root);
    }

    //if enter is pressed table of film will be filled with new content
    public void onEnter(){
        progressbar.setVisible(true);
        new Thread(() -> {
            try{
                makeFavouriteFileToString();
                makeBookmarksFileToString();
            } catch (Exception e){e.printStackTrace();}
            try{
                originalFilms = getFilm(correctStringForSearch(searchField.getText()));
            } catch (Exception e ){e.printStackTrace();}

            progressbar.setVisible(false);
            filmTable.setItems(originalFilms);

            //search result will be filtered according to selected years
            //AND categories. This is implemented in method filterTableViewAccToYears
            filterTableViewAccToYears(yearComboBox.getValue());

            addListenerToCheckBoxInFavouriteColumn();
            addListenerToCheckBoxInBookmarksColumn();
        }).start();
    }

    private void makeFavouriteFileToString(){
        try {
            File fileFavourite = new File("Favoriten.txt");
            filmsInFavouriteAsString = HelperMethods.makeFileToString(fileFavourite);
        } catch (NullPointerException e) {
            filmsInFavouriteAsString = "";
        }
    }

    private void makeBookmarksFileToString(){
        try{
            File fileBookmarks = new File ("Bookmarks.txt");
            filmsInBookmarksAsString = HelperMethods.makeFileToString(fileBookmarks);
        } catch (NullPointerException e) {
            filmsInBookmarksAsString = "";
        }
    }

    private String correctStringForSearch(String keyword){
        String pattern = "\\s+";
        return keyword.replaceAll(pattern, "+");
    }

    private ObservableList<Film> getFilm(String searchCorrection) {
        ObservableList<Film> films = FXCollections.observableArrayList();
        Results r = MovieDatabase.getMoviesByName(searchCorrection);
        r.getMovies()
                .stream()
                .map(movie ->
                        new Film(isThisMovieInFavourite(movie), movie.getTitle(), movie.getReleaseYear(),
                                movie.getOverview(), isThisMovieInBookmarks(movie), "", MovieDatabase.getPoster(movie),
                                movie.getCategories())
                )
                .forEach(films::add);
        return films;
    }

    //if favourite Checkbox is clicked the film will be written in a File
    private void addListenerToCheckBoxInFavouriteColumn(){
        for (Film film : originalFilms) {
            film.favouriteProperty().addListener((observableValue, oldValue, newValue) -> {
                if(newValue)
                    writeInFavourite(film.getTitle(), String.valueOf(film.getYear()));
                else if(!newValue)
                    deleteInFavourite(film.getTitle(), String.valueOf(film.getYear()));
            });
        }
    }

    //if remember Checkbox is clicked the film will be written in a File
    private void addListenerToCheckBoxInBookmarksColumn(){
        for (Film film : originalFilms) {
            film.rememberProperty().addListener((observableValue, oldValue, newValue) -> {
                if(newValue)
                    writeInBookmarks(film.getTitle(), String.valueOf(film.getYear()));
                else if(!newValue && oldValue)
                    deleteInBookmarks(film.getTitle(), String.valueOf(film.getYear()));
            });
        }
    }

    //write in Favoriten.txt to save checkbox action from favouriteColumn
    private void writeInFavourite(String filmTitle, String filmYear){
        HelperMethods.writeInFile("Favoriten.txt", filmTitle, filmYear);
    }

    //write in Bookmarks.txt to save checkbox action from rememberColumn
    private void writeInBookmarks(String filmTitle, String filmYear){
        HelperMethods.writeInFile("Bookmarks.txt", filmTitle, filmYear);
    }

    private void deleteInFavourite(String title, String year){
        File original = new File("Favoriten.txt");
        File copy = new File("copyOfFavourite.txt");
        HelperMethods.copyOriginalFileBesidesOneLine(original, copy, title, year);
        //At first we wanted to delete the original file and then rename the copy file, however the method delete()
        //does not work because of unknown reason. So this is our second best solution to resolve this issue.
        HelperMethods.overwriteSecondFileWithFirstFile(copy, original);
    }

    private void deleteInBookmarks(String title, String year){
        File original = new File("Bookmarks.txt");
        File copy = new File("copyOfBookmarks.txt");
        HelperMethods.copyOriginalFileBesidesOneLine(original, copy, title, year);
        HelperMethods.overwriteSecondFileWithFirstFile(copy, original);
    }

    private boolean isThisMovieInFavourite(Movie movie){
        String findMovieKeyword = movie.getTitle() + "\t" + movie.getReleaseYear();
        if(filmsInFavouriteAsString == null){
            filmsInFavouriteAsString = "";
        }
        Matcher matcher = Pattern.compile(findMovieKeyword).matcher(filmsInFavouriteAsString);
        return matcher.find();
    }

    private boolean isThisMovieInBookmarks(Movie movie){
        String findMovieKeyword = movie.getTitle() + "\t" + movie.getReleaseYear();
        if(filmsInBookmarksAsString == null){
            filmsInBookmarksAsString = "";
        }
        Matcher matcher = Pattern.compile(findMovieKeyword).matcher(filmsInBookmarksAsString);
        return matcher.find();
    }

    public void clickYearComboBox(){
        filterTableViewAccToYears(yearComboBox.getValue());
    }

    public void clickCategoriesComboBox() {
        filterTableViewAccToYears(yearComboBox.getValue());
    }

    //filter tableView list according to selected year
    private void filterTableViewAccToYears(String yearOfComboBox){
        ObservableList<Film> selectedYearFilms;
        selectedYearFilms = FXCollections.observableArrayList();
        try {
            int year = Integer.parseInt(yearOfComboBox);
            for (Film s : originalFilms){
                if (s.getYear() == year){
                    selectedYearFilms.add(s);
                }
            }
        } catch (NumberFormatException e) {
            selectedYearFilms = originalFilms;
        }
        originalFilmsForSecondFilterAction = selectedYearFilms;
        filterTableViewAccToCategories(categoriesComboBox.getValue());
        filmTable.setItems(originalFilmsForSecondFilterAction);
    }

    //filter tableView list according to selected category
    private void filterTableViewAccToCategories(String categoryOfComboBox) {
        ObservableList<Film> selectedCategoriesFilms;
        selectedCategoriesFilms = FXCollections.observableArrayList();
        if(categoryOfComboBox == null){ }
        else if (categoryOfComboBox.equals("Alle")) { }
        else {
            for (Film s : originalFilmsForSecondFilterAction) {
                Matcher matcher = Pattern.compile(categoryOfComboBox).matcher(s.getCategories());
                if (matcher.find())
                    selectedCategoriesFilms.add(s);
                }
            originalFilmsForSecondFilterAction = selectedCategoriesFilms;
            }
    }
        /*
        AutoCompletionBinding<MovieData> autoCompletionBinding =
                TextFields.bindAutoCompletion(searchField,
                        suggestionRequest -> MovieData. );
        autoCompletionBinding.setOnAutoCompleted(event -> {
            cities.add(event.getCompletion());
            searchField.clear();
            saveCitiesToDisk();
        });*/
}