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
import org.l2jmobius.commons.util.StringUtil;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.handler.IParseBoardHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Arrays;
import java.util.stream.Collectors;


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
    private static final String LOAD_PVP_HTML = "SELECT * FROM characters WHERE accesslevel=0 ORDER BY pvpkills DESC LIMIT 0, 15";
    private static final String LOAD_FAME_HTML = "SELECT * FROM characters WHERE accesslevel=0 ORDER BY fame DESC LIMIT 0, 15";
    private static final String LOAD_PK_HTML = "SELECT * FROM characters WHERE accesslevel=0 ORDER BY pkkills DESC LIMIT 0, 15";
    private static final String LOAD_ONLINE_HTML = "SELECT * FROM characters WHERE accesslevel=0 ORDER BY online DESC LIMIT 0, 15";

    private static final String[] COMMANDS =
            {
                    "_bbspvp",
                    "_bbsfame",
                    "_bbsgetfav",
                    "_bbspk",
                    "_bbsonline",
                    "_bbsclanlist",
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
        else if (command.startsWith("_bbsclanlist"))
        {
            new ClanBoard().parseCommunityBoardCommand("_bbsclanlist;1", player);

            return true;
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



        HTML.append("<html noscrollbar>");
        HTML.append("<body>");
        HTML.append("<table border=0 cellpadding=5 cellspacing=2>");
        HTML.append("<tr>");
        HTML.append("<td>");

        HTML.append("<table>");
        HTML.append("<tr>");
        HTML.append("<td>");

        HTML.append("<table cellpadding=4 cellspacing=2 width=102 height=32 background=L2UI_Reborn.html.Bg1>");
        HTML.append("<tr>");
        HTML.append("<td align=center >");

        HTML.append("<table cellpadding=0 cellspacing=0>");
        HTML.append("<tr>");
        HTML.append("<td height=28 align=center>");
        HTML.append("<button align=center value=\"Top PvP\" action=\"bypass _bbspvp\" width=90 height=20 back=L2UI_NewTex.SimpleBtnGreen_DF fore=L2UI_NewTex.SimpleBtnGreen_Over>");
        HTML.append("</td>");
        HTML.append("<td height=28 align=center>");
        HTML.append("<button align=center value=\"Top PK\" action=\"bypass _bbspk\" width=90 height=20 back=L2UI_NewTex.SimpleBtnBrown_DF fore=L2UI_NewTex.SimpleBtnBrown_Over>");
        HTML.append("</td>");

        HTML.append("</tr>");
        HTML.append("</table>");


        HTML.append("</td>");
        HTML.append("</tr>");
        HTML.append("</table>");

        HTML.append("</td>");
        HTML.append("</tr>");
        HTML.append("</table>");

        HTML.append("<table cellpadding=0 cellspacing=0><tr><td height=7></td></tr></table>");

        HTML.append("<table width=20>");
        HTML.append("<tr>");
        HTML.append("<td>");

        HTML.append("<table border=0 cellpadding=2 cellspacing=2 width=759 height=450 background=L2UI_Reborn.html.Bg1>");
        HTML.append("<tr><td height=2></td></tr>");

        HTML.append("<tr>");
        HTML.append("<td align=center>");
        HTML.append("<table border=0 cellspacing=0 cellpadding=0 height=25>");
        HTML.append("<tr>");
        HTML.append("<td align=center>");
        HTML.append("<table cellspacing=0 height=25 bgcolor=030202>");
        HTML.append("<tr>");
        HTML.append("<td width=1></td>");
        HTML.append("<td width=2 align=center height=32></td>");
        HTML.append("<td align=left width=30><font color=6f6f6f>#</font></td>");
        HTML.append("<td width=2 align=center height=32></td>");
        HTML.append("<td align=left width=209><font color=6f6f6f>Nickname</font></td>");
        HTML.append("<td width=2 align=center height=32></td>");
        HTML.append("<td align=left width=157><font color=6f6f6f>Clan</font></td>");
        HTML.append("<td width=2 align=center height=32></td>");
        HTML.append("<td align=left width=159><font color=6f6f6f>Main Class</font></td>");
        HTML.append("<td width=2 align=center height=32></td>");
        HTML.append("<td align=left width=171><font color=6f6f6f>PvP Count</font></td>");
        HTML.append("</tr>");
        HTML.append("</table>");


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

            int rank = 1; // Ξεκινάει από 1

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


//                String baseclass = ClassId.getClassId(rs.getInt("base_class")).name().toLowerCase();
//
//// Replace underscores with spaces and capitalize each word properly
//                baseclass = Arrays.stream(baseclass.replace("_", " ").split(" "))
//                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
//                        .collect(Collectors.joining(" "));

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


                String rankDisplay = "";
                if (rank == 1) {
                    rankDisplay = "<img src=L2UI_EPIC.SuppressWnd.RankingWnd_1stSmall width=24 height=24>";
                } else if (rank == 2) {
                    rankDisplay = "<img src=L2UI_EPIC.SuppressWnd.RankingWnd_2ndSmall width=24 height=24>";
                } else if (rank == 3) {
                    rankDisplay = "<img src=L2UI_EPIC.SuppressWnd.RankingWnd_3rdSmall width=24 height=24>";
                } else {
                    rankDisplay = "<span style=\"display:inline-block; width:24px; height:24px; line-height:24px; text-align:center; color:#FFFFFF; font-weight:bold;\">" + rank + "</span>";
                }





                HTML.append("<table cellspacing=0 height=25 " + (rank % 2 == 0 ? "bgcolor=\"030202\"" : "") + ">");
                HTML.append("<tr>");
                HTML.append("<td width=1></td>");
                HTML.append("<td align=center  width=30>" + rankDisplay + "</td>");
                HTML.append("<td width=2 align=center height=32></td>");
                HTML.append("<td align=left width=209><font color=edac51>" + name + "</font></td>");
                HTML.append("<td width=2 align=center height=32></td>");
                HTML.append("<td align=left width=157><font color=d0d0d0>" + clanname + "</font></td>");
                HTML.append("<td width=2 align=center height=32></td>");
                HTML.append("<td></td>");
                HTML.append("<td width=2 align=center height=32></td>");
                HTML.append("<td align=left width=171><font color=f101f1>" + pvps + "</font></td>");

                HTML.append("</tr>");
                HTML.append("</table>");
                rank++;


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

            HTML.append("</td>");
            HTML.append("</tr>");
            HTML.append("</table>");

            HTML.append("</td>");
            HTML.append("</tr>");
            HTML.append("<tr>");
            HTML.append("<td height=10></td>");
            HTML.append("</tr>");
            HTML.append("</table>");

            HTML.append("<table border=0 cellpadding=0 cellspacing=0 width=555>");
            HTML.append("<tr>");
            HTML.append("<td height=10></td>");
            HTML.append("</tr>");
            HTML.append("</table>");

            HTML.append("</td>");
            HTML.append("</tr>");
            HTML.append("</table>");

            HTML.append("</td>");
            HTML.append("</tr>");
            HTML.append("</table>");
            HTML.append("</body></html>");
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

    private String generateClanBoardHTML()
    {
        ClanBoard cb = new ClanBoard();
        return cb.parseCommunityBoardCommand("_bbsclanlist;1", _player) ? null : "";
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
        return PvpBoard.SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder
    {
        protected static final PvpBoard INSTANCE = new PvpBoard();
    }
}


