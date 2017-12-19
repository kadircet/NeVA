<?php

session_start();

if(!isset($_SESSION['loggedin']) || !$_SESSION['loggedin']) {
  $_SESSION['loggedin'] = false;
  if(isset($_POST['login'])) {
    if(md5($_POST['password']) == "40ad8fc9442f32397834d1971abfb38e") {
      $_SESSION['loggedin'] = true;
    } else {
      echo "<b>Wrong password.</b>";
    }
  }
  if(!$_SESSION['loggedin']) {
    echo <<<EOF
<form method="POST">
  <input type="password" name="password" />
  <input type="submit" name="login" />
</form>
EOF;
    die();
  }
}

$host = "localhost";
$user = "neva";
$pass = "";
$name = "neva";

mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);
$db = new mysqli($host, $user, $pass, $name);
if($db->connect_errno) {
  die("DB Connection: ".$db->connect_error);
}
unset($host, $user, $pass, $name);

if(isset($_POST['accept']) || isset($_POST['reject'])) {
  if(isset($_POST['accept'])) {
    $name = $_POST['suggestion'];
    $category = (int)$_POST['category_id'];
    $sql = "INSERT INTO `suggestee` (`category_id`, `name`) VALUES (?, ?)";
    $stmt = $db->prepare($sql);
    $stmt->bind_param("is", $category, $name);
    $stmt->execute();
  }
  $prop_id = (int)$_POST['id'];
  $sql = "DELETE FROM `item_suggestion` WHERE `id`=?";
  $stmt = $db->prepare($sql);
  $stmt->bind_param("i", $prop_id);
  $stmt->execute();
}

$sql = "SELECT items.`id`, `name`, `suggestion`, cats.`id` FROM
  `item_suggestion` items, `suggestion_category` cats WHERE
  items.`category_id` = cats.`id`";
$res = $db->query($sql);
if($res->num_rows==0) {
  echo "Nothing to moderate, well done.";
}

echo "<table>";
while($prop = $res->fetch_array()) {
  echo <<<EOF
<tr>
<form method="POST">
  <input type="hidden" name="id" value="$prop[0]">
  <td><label>$prop[1]</label></td>
  <td><input type="text" name="suggestion" value="$prop[2]"></td>
  <td><input type="submit" name="accept" value="accept"></td>
  <td><input type="submit" name="reject" value="reject"></td>
  <input type="hidden" name="category_id" value="$prop[3]">
</form>
</tr>
EOF;
}
echo "</table>";