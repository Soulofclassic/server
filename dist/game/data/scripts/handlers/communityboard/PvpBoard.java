/*
 * This file is part of the L2J Mobius project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.communityboard;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.handler.IParseBoardHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Acacia
 */
public class PvpBoard implements IParseBoardHandler
{
    Player _player = null;
    protected static StringBuilder _pvpHTML = null;
    protected static StringBuilder _fameHTML = null;
    protected static StringBuilder _pkHTML = null;
    protected static StringBuilder _onlineHTML = null;
    private static final String LOAD_PVP_HTML = "SELECT * FROM characters WHERE accesslevel=0 ORDER BY pvpkills DESC LIMIT 0, 25";
    private static final String LOAD_FAME_HTML = "SELECT * FROM characters WHERE accesslevel=0 ORDER BY fame DESC LIMIT 0, 25";
    private static final String LOAD_PK_HTML = "SELECT * FROM characters WHERE accesslevel=0 ORDER BY pkkills DESC LIMIT 0, 25";
    private static final String LOAD_ONLINE_HTML = "SELECT * FROM characters WHERE accesslevel=0 ORDER BY online DESC LIMIT 0, 25";

    private static final String[] COMMANDS =
            {
                    "_bbspvp",
                    "_bbsfame",
                    "_bbsgetfav",
                    "_bbspk",
                    "_bbsonline"
            };

    @Override
    public boolean parseCommunityBoardCommand(String command, Player player)
    {

        _player = player;

        if (command.startsWith("_bbspvp") || command.startsWith("_bbsgetfav"))
        {

            String html = _pvpHTML.toString();
            html = html.replace("%PvP%", "PvP");
            html = html.replace("%Fame%", "<a action=\"bypass _bbsfame\">Fame</a>");
            html = html.replace("%PK%", "<a action=\"bypass _bbspk\">PK</a>");
            html = html.replace("%ONLINE%", "<a action=\"bypass _bbsonline\">Online</a>");

            CommunityBoardHandler.separateAndSend(html, player);
        }
        else if (command.startsWith("_bbsfame"))
        {

            String html = _fameHTML.toString();
            html = html.replace("%PvP%", "<a action=\"bypass _bbspvp\">PvP</a>");
            html = html.replace("%Fame%", "Fame");
            html = html.replace("%PK%", "<a action=\"bypass _bbspk\">PK</a>");
            html = html.replace("%ONLINE%", "<a action=\"bypass _bbsonline\">Online</a>");

            CommunityBoardHandler.separateAndSend(html, player);
        }
        else if (command.startsWith("_bbspk"))
        {

            String html = _pkHTML.toString();
            html = html.replace("%PvP%", "<a action=\"bypass _bbspvp\">PvP</a>");
            html = html.replace("%Fame%", "<a action=\"bypass _bbsfame\">Fame</a>");
            html = html.replace("%PK%", "PK");
            html = html.replace("%ONLINE%", "<a action=\"bypass _bbsonline\">Online</a>");

            CommunityBoardHandler.separateAndSend(html, player);
        }
        else if (command.startsWith("_bbsonline"))
        {

            String html = _onlineHTML.toString();
            html = html.replace("%PvP%", "<a action=\"bypass _bbspvp\">PvP</a>");
            html = html.replace("%Fame%", "<a action=\"bypass _bbsfame\">Fame</a>");
            html = html.replace("%PK%", "<a action=\"bypass _bbspk\">PK</a>");
            html = html.replace("%ONLINE%", "Online");

            CommunityBoardHandler.separateAndSend(html, player);
        }

        else
        {
            ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
            player.sendPacket(sb);

        }
        return false;
    }

    @Override
    public String[] getCommunityBoardCommands()
    {
        return COMMANDS;
    }

    @SuppressWarnings("null")
    public void loadHTML(int whichone)
    {
        final StringBuilder HTML = new StringBuilder(1000);

        String info = null;
        switch (whichone)
        {
            default:
                info = "Top PvPers of the server (updated every 5 minutes)";
                break;
            case 1:
                info = "Most famous people of the server (updated every 5 minutes)";
                break;
            case 2:
                info = "The most \"hardcore\" players (updated every 5 minutes)";
                break;
            case 3:
                info = "Online List (updated every 5 minutes)";
                break;
        }

        HTML.append("<html><title>" + info + "</title><body><br><center><table width=\"100%\">");

        HTML.append("<tr>");

        HTML.append("<td><font color=\"LEVEL\">Player Name</font></td>");
        HTML.append("<td><font color=\"LEVEL\">Player Title</font></td>");
        HTML.append("<td><font color=\"LEVEL\">Base Class</font></td>");
        HTML.append("<td><font color=\"LEVEL\">%ONLINE%</font></td>");
        HTML.append("<td><font color=\"LEVEL\">Clan</font></td>");

        HTML.append("<td><font color=\"LEVEL\">%Fame%</font></td>");
        HTML.append("<td><font color=\"LEVEL\">%PvP%</font></td>");
        HTML.append("<td><font color=\"LEVEL\">%PK%</font></td>");

        HTML.append("</tr>");

        PreparedStatement statement;
        ResultSet rs;
        Connection con = null;
        try
        {
            con = DatabaseFactory.getConnection();

            switch (whichone)
            {
                default:
                    statement = con.prepareStatement(LOAD_PVP_HTML);
                    break;
                case 1:
                    statement = con.prepareStatement(LOAD_FAME_HTML);
                    break;
                case 2:
                    statement = con.prepareStatement(LOAD_PK_HTML);
                    break;
                case 3:
                    statement = con.prepareStatement(LOAD_ONLINE_HTML);
                    break;
            }

            boolean lol = true;
            String color = "FFF8C6";
            String colorName = "bdccd4";
            String colorClass = "bdccd4";
            String colorOnline = "bdccd4";
            String colorPvPs = "bdccd4";
            String colorFame = "686868";

            rs = statement.executeQuery();

            while (rs.next())
            {
                String name = rs.getString("char_name");
                String title = rs.getString("title");
                if (title == null)
                {
                    title = "";
                }
                title = title.replaceAll("<", "&lt;");
                title = title.replaceAll(">", "&gt;");

                String fame = String.valueOf(rs.getInt("fame"));
                String pvps = String.valueOf(rs.getInt("pvpkills"));
                String pks = String.valueOf(rs.getInt("pkkills"));

              //  ClassId baseclass = ClassId.getClassId(rs.getInt("base_class"));
                boolean online = Boolean.parseBoolean(String.valueOf(rs.getBoolean("online")));

                if(rs.getBoolean("online"))
                {
                    colorName = "379d2d";
                    colorClass = "916e27";
                    colorOnline = "379d2d";
                    colorPvPs = "8c4848";
                    colorFame = "744ccf";
                }
                else {
                    colorName = "bdccd4";
                    colorClass = "bdccd4";
                    colorOnline = "bdccd4";
                    colorPvPs = "bdccd4";
                    colorFame = "686868";
                }

                if(rs.getBoolean("online"))
                    name = "<font color="+colorName+">"+name+"</font>";

                Clan clan = ClanTable.getInstance().getClan(rs.getInt("clanid"));
                String clanname = "-";
                if (clan != null)
                {
                    clanname = clan.getName() + " (Lvl " + clan.getLevel() + ")";
                }

                HTML.append("<tr>");
                HTML.append("<td><font color=" + colorName + ">" + name + "</font></td>");
                HTML.append("<td><font color=" + color + ">" + title + "</font></td>");
                HTML.append("<td></td>");
                HTML.append("<td><font color=" + colorOnline + ">" + online + "</font></td>");
                HTML.append("<td><font color=" + color + ">" + clanname + "</font></td>");

                HTML.append("<td><font color=" + colorFame + ">" + fame + "</font></td>");
                HTML.append("<td><font color=" + colorPvPs + ">" + pvps + "</font></td>");
                HTML.append("<td><font color=" + color + ">" + pks + "</font></td>");
                HTML.append("</tr>");

                if (lol)
                {
                    lol = false;
                    color = "817679";
                }
                else
                {
                    lol = true;
                    color = "FFF8C6";
                }
            }

            rs.close();
            statement.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }

            HTML.append("</table></center><br></body></html>");
        }

        switch (whichone)
        {
            default:
                _pvpHTML = HTML;
                break;
            case 1:
                _fameHTML = HTML;
                break;
            case 2:
                _pkHTML = HTML;
                break;
            case 3:
                _onlineHTML = HTML;
                break;

        }
    }

    public PvpBoard() {
        ThreadPool.scheduleAtFixedRate(new Start(), 0 ,10000); //5 minutes
    }

    class Start implements Runnable
    {
        @Override
        public void run()
        {
            loadHTML(0);
            loadHTML(1);
            loadHTML(2);
            loadHTML(3);
        }
    }

    public static PvpBoard getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder
    {
        protected static final PvpBoard INSTANCE = new PvpBoard();
    }
}