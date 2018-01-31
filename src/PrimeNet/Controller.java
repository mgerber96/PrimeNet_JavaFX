package PrimeNet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.DefaultStringConverter;


public class Controller{
    @FXML
    ComboBox<String> categoriesComboBox;
    @FXML
    TextField searchField = new TextField();
    TableView<Film> originalTableofFilm = new TableView();
    @FXML
    TableView<Film> tableofFilm = new TableView<>();
    @FXML
    TableColumn<Film, Boolean> favouriteColumn = new TableColumn<>("Favorit");
    @FXML
    TableColumn<Film, String> titleColumn = new TableColumn<>("Film");
    @FXML
    TableColumn<Film, Integer> yearColumn = new TableColumn<>("Jahr");
    @FXML
    TableColumn<Film, Boolean> rememberColumn = new TableColumn<>("Merken");
    @FXML
    ComboBox<Integer> yearComboBox = new ComboBox<>();
    @FXML
    TableColumn<Film, String> rateColumn = new TableColumn<>("Bewertung");
    @FXML
    private ObservableList<String> rating = FXCollections.observableArrayList();


    @FXML
    public void initialize(){
        setUpEverything();
        rating.addAll("Like", "Dislike");


    }

    public void setUpEverything(){
        fillCategoriesComboBox();
        fillYearComboBox();
        setUpTableOfFilm();
    }

    public void fillCategoriesComboBox(){
        categoriesComboBox.getItems().addAll("Action","Abenteuer", "Animation", "Drama", "Familie", "Fantasie",
                "Historie", "Horror", "Kriegsfilm", "Krimi", "Komödie", "Liebesfilm", "Science Fiction");
    }

    public void fillYearComboBox(){
        yearComboBox.getItems().addAll(2015,2016,2017);
    }

    public void setUpTableOfFilm() {
        tableofFilm.setEditable(true);

        //titleColumn
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        //yearColumn
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        //rememberColumn
        rememberColumn.setCellValueFactory(new PropertyValueFactory<>("remember"));
        rememberColumn.setCellFactory(CheckBoxTableCell.forTableColumn(rememberColumn));
        rememberColumn.setEditable(true);

        //favouriteColumn
        favouriteColumn.setCellValueFactory(new PropertyValueFactory<>("favourite"));
        favouriteColumn.setCellFactory(CheckBoxTableCell.forTableColumn(favouriteColumn));
        favouriteColumn.setEditable(true);

        //rateColumn
        rateColumn.setCellValueFactory(new PropertyValueFactory<>("rate"));
        rateColumn.setCellFactory(ComboBoxTableCell.forTableColumn(new DefaultStringConverter(),rating));

    }

    //if enter is pressed table of film will be filled with new content
    public void onEnter() {
        searchField.clear();
        originalTableofFilm.setItems(getFilm());
        tableofFilm.setItems(getFilm());
        tableofFilm.getColumns().addAll(favouriteColumn, titleColumn, yearColumn, rememberColumn,rateColumn);



        /*//detect double click on item
        tableofFilm.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent doubleclick) {
                if (doubleclick.isPrimaryButtonDown() && doubleclick.getClickCount() == 2) {
                    Labelmoviename.setText()}

            }
        });*/
    }

    public ObservableList<Film> getFilm() {
        ObservableList<Film> films = FXCollections.observableArrayList();
        films.add(new Film(false, "Legend of Tarzan", 2016, false, "      -"));
        films.add(new Film(false, "Zoomania", 2017, false,  "      -" ));
        films.add(new Film(false, "Batman v Superman", 2016, false,"      -" ));
        films.add(new Film(false, "Suicide Squad", 2016, false,"      -" ));
        films.add(new Film(false, "Teenage Mutant Ninja Turtles", 2016, false,"      -"));
        return films;
    }

    //filter tableView list according to selected year
    public void filterTableView(int i){
        ObservableList<Film> allFilms, selectedYearFilms;
        allFilms = originalTableofFilm.getItems();
        selectedYearFilms = FXCollections.observableArrayList();
        for (Film s : allFilms){
            if (s.getYear() == i){
                selectedYearFilms.add(s);
            }
        }
        tableofFilm.setItems(selectedYearFilms);
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





