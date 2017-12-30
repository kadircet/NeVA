<?php

session_start();

if(isset($_GET['error'])) {
  switch($_GET['error']) {
  case 1:
    echo "<b>Wrong password.</b>";
    break;
  default:
    echo "<b>An unknown error occured.</b>";
    break;
  }
}

if(!isset($_SESSION['loggedin']) || !$_SESSION['loggedin']) {
  $_SESSION['loggedin'] = false;
  if(isset($_POST['login'])) {
    if(md5($_POST['password']) == "40ad8fc9442f32397834d1971abfb38e") {
      $_SESSION['loggedin'] = true;
      header('Location: /moderation.php');
    } else {
      header('Location: /moderation.php?error=1');
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

if(isset($_POST['accept_tag']) || isset($_POST['reject_tag'])) {
  if(isset($_POST['accept_tag'])) {
    $name = $_POST['suggestion'];
    $sql = "INSERT INTO `tag` (`key`) VALUES (?)";
    $stmt = $db->prepare($sql);
    $stmt->bind_param("s", $name);
    $stmt->execute();
  }
  $prop_id = (int)$_POST['id'];
  $sql = "DELETE FROM `tag_suggestion` WHERE `id`=?";
  $stmt = $db->prepare($sql);
  $stmt->bind_param("i", $prop_id);
  $stmt->execute();
}

$sql = "SELECT `category_id`, `name` FROM `suggestee`";
$res = $db->query($sql);
$suggestees = array();
while($suggestee = $res->fetch_array()) {
  $category_id = $suggestee[0];
  $name = $suggestee[1];
  if(!array_key_exists($category_id, $suggestees)) {
    $suggestees[$category_id] = new \Ds\Set();
  }
  $suggestees[$category_id]->add($name);
}

$sql = "SELECT items.`id`, `name`, `suggestion`, cats.`id` FROM
  `item_suggestion` items, `suggestion_category` cats WHERE
  items.`category_id` = cats.`id`";
$res = $db->query($sql);
if($res->num_rows==0) {
  echo "Nothing to moderate, well done.";
}

echo "<table style='float: left;'>";
while($prop = $res->fetch_array()) {
  $exists = $suggestees[$prop[3]]->contains($prop[2]);
  $color = $exists ? 'red' : 'green';
  echo <<<EOF
<tr style='background: $color;'>
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
echo <<<EOF
<tr>
<form method="POST">
  <input type="hidden" name="id" value="0">
  <td><label>meal</label></td>
  <td><input type="text" name="suggestion" value=""></td>
  <td><input type="submit" name="accept" value="add meal"></td>
  <input type="hidden" name="category_id" value="1">
</form>
</tr>
EOF;
echo "</table>";

$sql = "SELECT `id`, `tag` FROM `tag_suggestion` tags";
$res = $db->query($sql);
if($res->num_rows==0) {
  echo "No tag to moderate, well done.";
}

echo "<table style='float: left;'>";
while($prop = $res->fetch_array()) {
  echo <<<EOF
<tr>
<form method="POST">
  <input type="hidden" name="id" value="$prop[0]">
  <td><label>tag</label></td>
  <td><input type="text" name="suggestion" value="$prop[1]"></td>
  <td><input type="submit" name="accept_tag" value="accept"></td>
  <td><input type="submit" name="reject_tag" value="reject"></td>
</form>
</tr>
EOF;
}
echo <<<EOF
<tr>
<form method="POST">
  <input type="hidden" name="id" value="0">
  <td><label>tag</label></td>
  <td><input type="text" name="suggestion" value=""></td>
  <td><input type="submit" name="accept_tag" value="add tag"></td>
</form>
</tr>
EOF;
echo "</table>";
