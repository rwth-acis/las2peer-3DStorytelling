package i5.las2peer.services.story3DSemanticCheck.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONObject;

import i5.cae.simpleModel.SimpleEntityAttribute;

/**
 * 
 * (Data-)Class for EntityAttributes. Provides means to convert JSON to Object and Object to JSON.
 * Also provides means to persist the object to a database.
 *
 * TODO care for different entity types (not only String!!)
 * 
 */
public class EntityAttribute {
  private int id = -1; // given by the database
  private String syncMetaId; // each attribute type shares the same id according to its metamodel
  private String name;
  private String value;

  /**
   * 
   * Creates a new EntityAttribute.
   * 
   * @param syncMetaId the id of the object created by SyncMeta
   * 
   * @param jsonAttribute the content of the attribute in the (JSON-represented) model
   * 
   */
  public EntityAttribute(String syncMetaId, JSONObject jsonAttribute) {
    // fetch "value" entry from attributes (JSON format is a bit redundant due to frontend reasons)
    jsonAttribute = (JSONObject) jsonAttribute.get("value");
    this.syncMetaId = syncMetaId;
    this.name = (String) jsonAttribute.get("name");
    this.value = (String) (jsonAttribute.get("value") + ""); // only store strings
  }


  /**
   * 
   * Creates a new EntityAttribute by loading it from the database.
   * 
   * @param attributeId the id of the attribute given by the database
   * 
   * @param connection a Connection object
   * 
   * @throws SQLException if something went wrong fetching the attribute from the database
   * 
   */
  public EntityAttribute(int attributeId, Connection connection) throws SQLException {
    // attributes entries
    PreparedStatement statement =
        connection.prepareStatement("SELECT * FROM Attribute WHERE attributeId = ?;");
    statement.setInt(1, attributeId);
    ResultSet queryResult = statement.executeQuery();
    if (queryResult.next()) {
      this.id = queryResult.getInt(1);
      this.syncMetaId = queryResult.getString(2);
      this.name = (String) queryResult.getString(3);
      this.value = (String) queryResult.getString(4);
      statement.close();
    } else {
      throw new SQLException("Could not find attribute");
    }
  }


  /**
   * 
   * Creates a new EntityAttribute from a passed {@link i5.cae.simpleModel.SimpleEntityAttribute}.
   * 
   * @param attribute a {@link i5.cae.simpleModel.SimpleEntityAttribute}
   * 
   */
  public EntityAttribute(SimpleEntityAttribute attribute) {
    this.syncMetaId = attribute.getSyncMetaId();
    this.name = attribute.getName();
    this.value = attribute.getValue();
  }


  public int getId() {
    return id;
  }


  public String getSyncMetaId() {
    return syncMetaId;
  }


  public String getName() {
    return name;
  }


  public String getValue() {
    return value;
  }


  /**
   * 
   * Persists an EntityAttribute.
   * 
   * @param connection a Connection object
   * 
   * @throws SQLException if something went wrong persisting the entity attribute
   * 
   */
  public void persist(Connection connection) throws SQLException {
    // Attribute entry
    PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO Attribute (syncMetaId, name, value) VALUES (?,?,?);",
        Statement.RETURN_GENERATED_KEYS);
    statement.setString(1, this.syncMetaId);
    statement.setString(2, this.name);
    statement.setString(3, this.value);
    statement.executeUpdate();
    ResultSet genKeys = statement.getGeneratedKeys();
    genKeys.next();
    // set given id
    this.id = genKeys.getInt(1);
    statement.close();
  }


  /**
   * 
   * Deletes this EntityAttribute from the database.
   * 
   * @param connection a ConnectionObject
   * 
   * @throws SQLException if something went wrong during deletion
   * 
   */
  public void deleteFromDatabase(Connection connection) throws SQLException {
    PreparedStatement statement =
        connection.prepareStatement("DELETE FROM Attribute WHERE attributeId = ?;");
    statement.setInt(1, this.id);
    statement.executeUpdate();
    statement.close();
  }
}
