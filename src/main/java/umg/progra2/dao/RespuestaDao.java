package umg.progra2.dao;

import umg.progra2.db.DatabaseConnection;
import umg.progra2.model.Respuesta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RespuestaDao {

    public void deleteRespuestaById(int id) throws SQLException {
        String query = "DELETE FROM tb_respuestas WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void updateRespuesta(Respuesta respuesta) throws SQLException {
        String query = "UPDATE tb_respuestas SET seccion = ?, telegram_id = ?, pregunta_id = ?, respuesta_texto = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, respuesta.getSeccion());
            statement.setLong(2, respuesta.getTelegramId());
            statement.setInt(3, respuesta.getPreguntaId());
            statement.setString(4, respuesta.getRespuestaTexto());
            statement.setInt(5, respuesta.getId());
            statement.executeUpdate();
        }
    }

    public void insertRespuesta(Respuesta respuesta) throws SQLException {
        String query = "INSERT INTO tb_respuestas (seccion, telegram_id, pregunta_id, respuesta_texto) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, respuesta.getSeccion());
            statement.setLong(2, respuesta.getTelegramId());
            statement.setInt(3, respuesta.getPreguntaId());
            statement.setString(4, respuesta.getRespuestaTexto());
            statement.executeUpdate();
        }
    }

    public Respuesta getRespuestaById(int id) throws SQLException {
        String query = "SELECT * FROM tb_respuestas WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Respuesta respuesta = new Respuesta();
                respuesta.setId(resultSet.getInt("id"));
                respuesta.setSeccion(resultSet.getString("seccion"));
                respuesta.setTelegramId(resultSet.getLong("telegram_id"));
                respuesta.setPreguntaId(resultSet.getInt("pregunta_id"));
                respuesta.setRespuestaTexto(resultSet.getString("respuesta_texto"));
                respuesta.setFechaRespuesta(resultSet.getTimestamp("fecha_respuesta"));
                return respuesta;
            }
        }
        return null;
    }

}
